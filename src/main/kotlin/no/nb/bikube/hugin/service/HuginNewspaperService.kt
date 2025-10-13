package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import no.nb.bikube.hugin.model.ContactInfo
import no.nb.bikube.hugin.model.HuginTitle
import no.nb.bikube.hugin.model.dto.ContactUpdateDto
import no.nb.bikube.hugin.repository.TitleRepository
import org.springframework.data.repository.findByIdOrNull



@BrowserCallable
class HuginNewspaperService(
    private val titleRepository: TitleRepository
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

        return titleRepository.save(entity)
    }
}
