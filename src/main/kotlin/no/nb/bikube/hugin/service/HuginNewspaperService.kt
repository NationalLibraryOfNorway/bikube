package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.api.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.api.core.util.logger
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
import reactor.core.publisher.Mono

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
    fun upsertContactInformation(contactUpdateDto: ContactUpdateDto): HuginTitle {
        val huginTitle = titleRepository.findByIdOrNull(contactUpdateDto.id)
            ?: HuginTitle().apply { this.id = contactUpdateDto.id }

        contactUpdateDto.vendor?.let { huginTitle.vendor = it }
        contactUpdateDto.contactName?.let { huginTitle.contactName = it }
        contactUpdateDto.shelf?.let { huginTitle.shelf = it }
        contactUpdateDto.notes?.let { huginTitle.notes = it }

        // ContactInfos
        contactUpdateDto.contactInfos?.let { contactInfos ->
            huginTitle.contactInfos.clear()
            titleRepository.flush() // Ensure deletion of orphaned contact infos in DB
            contactInfos.forEach { ciDto ->
                logger().info("Adding contact info: ${ciDto.contactType} -> ${ciDto.contactValue}")
                huginTitle.contactInfos.add(
                    ContactInfo(
                        title = huginTitle,
                        contactType = ciDto.contactType,
                        contactValue = ciDto.contactValue
                    )
                )
            }
        }

        // Release pattern
        contactUpdateDto.releasePattern?.let { huginTitle.releasePattern = it.toTypedArray() }
        return titleRepository.save(huginTitle)
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun createBox(createBoxDto: CreateBoxDto): Box {
        val title = titleRepository.findByIdOrNull(createBoxDto.titleId)
            ?: error("Title ${createBoxDto.titleId} not found")
        // Deactivate all existing boxes for this title
        boxRepository.findAllByTitleIdOrderByDateFromAsc(createBoxDto.titleId)
            .onEach { it.active = false }
            .let { boxRepository.saveAll(it) }
        boxRepository.flush()
        // Create new active box
        return boxRepository.save(
            Box(
                id = createBoxDto.id.trim(),
                dateFrom = createBoxDto.dateFrom,
                active = true,
                title = title
            )
        )
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun upsertNewspaper(newspaperUpsertDto: List<NewspaperUpsertDto>): List<Newspaper> {
        val userName = (SecurityContextHolder.getContext().authentication.principal as OidcUser).preferredUsername
        val results = mutableListOf<Newspaper>()

        for (newspaperUpsert in newspaperUpsertDto) {
            val existingManifestation: Item? =
                newspaperService.getSingleManifestationAsItem(
                    newspaperUpsert.catalogId ?: ""
                ).onErrorResume { Mono.empty() }.block()

            if (existingManifestation === null) {
                if (newspaperUpsert.received === true) {
                    var item = newspaperService.createNewspaperItem(newspaperUpsert.toItemInputDto(userName)).block()
                        ?: error("Failed to create physical item")
                    results += saveNewspaperToDatabase(newspaperUpsert.toNewspaper(item.parentCatalogueId!!))
                } else {
                    var missingItem = newspaperService.createMissingItem(newspaperUpsert.toMissingDto(userName)).block()
                        ?: error("Failed to create missing manifestation")
                    results += saveNewspaperToDatabase(newspaperUpsert.toNewspaper(missingItem.catalogueId))
                }
            } else {
                var existingNewspaper = newspaperRepository.findByIdOrNull(existingManifestation.parentCatalogueId!!)
                    ?: newspaperUpsert.catalogId?.let { newspaperRepository.findByIdOrNull(it) }
                if (existingNewspaper == null) { // just in case vi er i usync med katalog
                    existingNewspaper =
                        newspaperRepository.save(newspaperUpsert.toNewspaper(existingManifestation.catalogueId!!))
                }
                if (newspaperUpsert.received === existingNewspaper.received) {
                    results += saveNewspaperToDatabase(newspaperUpsert.toNewspaper(existingNewspaper.catalogId))
                    newspaperService.updatePhysicalNewspaper(newspaperUpsert.toItemUpdateDto(userName)).block()
                } else if (existingNewspaper.received === false && newspaperUpsert.received === true) {
                    newspaperService.createNewspaperItem(newspaperUpsert.toItemInputDto(userName)).block()
                        ?: error("Failed to create physical item")
                    results += saveNewspaperToDatabase(newspaperUpsert.toNewspaper(existingNewspaper.catalogId))
                } else if (existingNewspaper.received === true && newspaperUpsert.received === false) {
                    newspaperService.deletePhysicalItemByManifestationId(existingNewspaper.catalogId, false)
                        .onErrorResume { Mono.empty() }
                        .block()
                    results += saveNewspaperToDatabase(newspaperUpsert.toNewspaper(existingManifestation.catalogueId))
                }
            }
        }
        return results;
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    @Transactional
    fun deleteNewspaper(manifestationId: String): Boolean {
        //delete physical item in catalog; tolerate "not found"
        newspaperService
            .deletePhysicalItemByManifestationId(manifestationId, true)
            .onErrorResume { Mono.empty() }
            .block()
        // delete DB row; tolerate missing row
        if (newspaperRepository.existsById(manifestationId)) {
            newspaperRepository.deleteById(manifestationId)
        }
        return true
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

    private fun NewspaperUpsertDto.toMissingDto(username: String) = MissingPeriodicalItemDto(
        date = date,
        titleCatalogueId = titleId.toString(),
        username = username,
        notes = notes,
        number = edition
    )
}
