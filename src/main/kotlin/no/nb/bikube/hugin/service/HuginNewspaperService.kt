package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.api.newspaper.service.NewspaperService
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
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser

@BrowserCallable
class HuginNewspaperService(
    private val titleRepository: TitleRepository,
    private val boxRepository: BoxRepository,
    private val newspaperService: NewspaperService,
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
    fun upsertNewspaper(dto: List<NewspaperUpsertDto>): Newspaper? {

        val userName = (SecurityContextHolder.getContext().authentication.principal as OidcUser).preferredUsername

        for (newspaperDto in dto) {
            println("Upserting newspaper: $newspaperDto")
            println("ItemUpdateDto: $newspaperDto.toItemUpdateDto(userName)")
            println("ItemInputDto: $newspaperDto.toItemInputDto(userName)")

            val existingCatalogueItem =
                if (newspaperDto.catalogId.isNullOrEmpty().not()) {
                    newspaperService
                        .getSingleItem(newspaperDto.catalogId!!)
                        .block()
                } else {
                    newspaperService
                        .getItemsByTitleAndDate(newspaperDto.titleId.toString(), newspaperDto.date, false)
                        .next()
                        .map { it as Item }
                        .block()
                }
            println("Existing item from catalog: $existingCatalogueItem")
            if (existingCatalogueItem === null) {
                // Utgave eksisterer ikke i katalogen, opprett ny i katalog og database
                val savedCatalogItem = newspaperService
                    .createNewspaperItem(newspaperDto.toItemInputDto(userName))
                    .block()
                    ?: error("Failed to create newspaper item")
                println("Created new catalog item with ID: $savedCatalogItem")
                return saveNewspaperToDatabase(newspaperDto.toNewspaper(savedCatalogItem.catalogueId))
            }
            else {
                // Utgave finnes allerede i katalogen, oppdater eksisterende i katalog og database
                // Hent først eksisterende avis i database, i alle normale fall skal denne eksistere
                val existingNewspaper = newspaperRepository.findById(existingCatalogueItem.catalogueId).get()

                if (newspaperDto.received === false && existingNewspaper.received === true) {
                            println("test")
                }

            }
        }
        return null;
    }

    private fun saveNewspaperToDatabase(newspaper: Newspaper): Newspaper {
        return newspaperRepository.save(newspaper)
    }

    private fun NewspaperUpsertDto.toItemInputDto(username: String) = ItemInputDto(
        date = date,
        titleCatalogueId = titleId.toString(),
        digital = false,
        containerId = boxId,
        notes = notes,
        number = edition,
        username = username,
    )

    private fun NewspaperUpsertDto.toItemUpdateDto(username: String) = ItemUpdateDto(
        manifestationId = catalogId!!,
        username = username,
        notes = notes,
        number = edition,
    )

    private fun NewspaperUpsertDto.toNewspaper(catalogId: String): Newspaper {
        val box = boxRepository.findByIdOrNull(boxId)
            ?: error("Box $boxId not found")

        return Newspaper(
            catalogId = catalogId,
            box = box,
            date = date,
            edition = edition,
            received = received,
            notes = notes,
        )

    }
}
