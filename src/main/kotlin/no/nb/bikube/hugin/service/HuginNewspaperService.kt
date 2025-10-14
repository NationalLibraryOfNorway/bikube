package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import no.nb.bikube.hugin.model.Box
import no.nb.bikube.hugin.model.ContactInfo
import no.nb.bikube.hugin.model.HuginTitle
import no.nb.bikube.hugin.model.Newspaper
import no.nb.bikube.hugin.model.dto.ContactUpdateDto
import no.nb.bikube.hugin.model.dto.CreateBoxDto
import no.nb.bikube.hugin.model.dto.NewspaperUpsertDto
import no.nb.bikube.hugin.repository.BoxRepository
import no.nb.bikube.hugin.repository.NewspaperRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

@BrowserCallable
class HuginNewspaperService(
    private val titleRepository: TitleRepository,
    private val boxRepository: BoxRepository,
    private val newspaperRepository: NewspaperRepository,
) {

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    fun getTitle(titleId: Int): HuginTitle? {
        return titleRepository.findById(titleId).orElse(null)
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun upsertContactInformation(dto: ContactUpdateDto): HuginTitle {
        val entity = titleRepository.findByIdOrNull(dto.id)
            ?: HuginTitle().apply { this.id = dto.id }

        dto.vendor?.let { entity.vendor = it }
        dto.contactName?.let { entity.contactName = it }
        dto.shelf?.let { entity.shelf = it }
        dto.notes?.let { entity.notes = it }

        // ContactInfos
        dto.contactInfos?.let { incoming ->
            entity.contactInfos.clear()
            titleRepository.flush() // Ensure deletion of orphaned contact infos in DB
            incoming.forEach { ciDto ->
                System.out.println("Adding contact info: ${ciDto.contactType} -> ${ciDto.contactValue}")
                val ci = ContactInfo(
                    title = entity,
                    contactType = ciDto.contactType,
                    contactValue = ciDto.contactValue
                )
                entity.contactInfos.add(ci)
            }
        }

        // Release pattern
        dto.releasePattern?.let { entity.releasePattern = it.toTypedArray() }
        return titleRepository.save(entity)
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun createBox(dto: CreateBoxDto): Box {
        val title = titleRepository.findByIdOrNull(dto.titleId)
            ?: error("Title ${dto.titleId} not found")
        // Deactivate all existing boxes for this title
        boxRepository.findAllByTitleIdOrderByDateFromAsc(dto.titleId)
            .onEach { it.active = false }
            .let { boxRepository.saveAll(it) }
        boxRepository.flush()
        // Create new active box
        val box = Box(
            id = dto.id.trim(),
            dateFrom = dto.dateFrom,
            active = true,
            title = title
        )

        return boxRepository.save(box)
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun addNewspaper(dto: NewspaperUpsertDto): Newspaper {
        val box = boxRepository.findByIdOrNull(dto.boxId)
            ?: error("Box ${dto.boxId} not found")

        if (box.title?.id != dto.titleId) error("Box/title mismatch")

        // Resolve date: dto.date -> last+1 -> box.dateFrom
        val date: LocalDate = dto.date
            ?: newspaperRepository.findTopByBoxIdOrderByDateDesc(box.id)?.date?.plusDays(1)
            ?: box.dateFrom
            ?: error("Box.dateFrom is null, cannot infer newspaper date")

        // Optional: ensure one per day per box
        if (newspaperRepository.existsByBoxIdAndDate(box.id, date)) {
            error("Newspaper already exists for $date in box ${box.id}")
        }

        // Derive/generate catalogId if not provided
        val catalogId = (dto.catalogId ?: "${box.id}-$date").trim()
        require(catalogId.isNotBlank()) { "catalogId is blank" }

        val n = Newspaper(
            catalogId = catalogId,
            edition = dto.edition?.trim(),
            date = date,
            received = dto.received,
            username = null,
            notes = dto.notes?.trim().takeUnless { it.isNullOrBlank() },
            box = box
        )

        // No cascade on Box.newspapers → save via repo
        val saved = newspaperRepository.save(n)

        // Keep inverse list in memory (optional, for UI freshness)
        box.newspapers.add(saved)

        return saved
    }
}
