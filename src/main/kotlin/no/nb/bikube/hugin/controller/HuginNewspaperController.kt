package no.nb.bikube.hugin.controller

import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.api.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.api.core.util.logger
import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.hugin.model.dbo.Box
import no.nb.bikube.hugin.model.dbo.ContactInfo
import no.nb.bikube.hugin.model.dbo.HuginTitle
import no.nb.bikube.hugin.model.dbo.Newspaper
import no.nb.bikube.hugin.model.dto.ContactUpdateDto
import no.nb.bikube.hugin.model.dto.CreateBoxDto
import no.nb.bikube.hugin.model.dto.NewspaperUpsertDto
import no.nb.bikube.hugin.repository.BoxRepository
import no.nb.bikube.hugin.repository.NewspaperRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/hugin")
class HuginNewspaperController(
    private val titleRepository: TitleRepository,
    private val boxRepository: BoxRepository,
    private val newspaperService: NewspaperService,
    private val newspaperRepository: NewspaperRepository,
) {

    @GetMapping("/titles/{titleId}")
    @RolesAllowed("T_dimo_admin", "T_dimo_all")
    suspend fun getTitle(@PathVariable titleId: Int): ResponseEntity<HuginTitle> =
        withContext(Dispatchers.IO) {
            titleRepository.findById(titleId).orElse(null)
                ?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity.notFound().build()
        }

    @PostMapping("/boxes")
    @RolesAllowed("T_dimo_admin", "T_dimo_all")
    @Transactional
    suspend fun createBox(@RequestBody createBoxDto: CreateBoxDto): Box =
        withContext(Dispatchers.IO) {
            val title = titleRepository.findByIdOrNull(createBoxDto.titleId)
                ?: error("Title ${createBoxDto.titleId} not found")
            boxRepository.findAllByTitleIdOrderByDateFromAsc(createBoxDto.titleId)
                .onEach { it.active = false }
                .let { boxRepository.saveAll(it) }
            boxRepository.flush()
            boxRepository.save(
                Box(
                    id = createBoxDto.id.trim(),
                    dateFrom = createBoxDto.dateFrom,
                    active = true,
                    title = title,
                )
            )
        }

    @PutMapping("/titles/contact")
    @RolesAllowed("T_dimo_admin", "T_dimo_all")
    @Transactional
    suspend fun upsertContactInformation(@RequestBody contactUpdateDto: ContactUpdateDto): HuginTitle =
        withContext(Dispatchers.IO) {
            val huginTitle = titleRepository.findByIdOrNull(contactUpdateDto.id)
                ?: HuginTitle().apply { this.id = contactUpdateDto.id }
            contactUpdateDto.vendor?.let { huginTitle.vendor = it }
            contactUpdateDto.contactName?.let { huginTitle.contactName = it }
            contactUpdateDto.shelf?.let { huginTitle.shelf = it }
            contactUpdateDto.notes?.let { huginTitle.notes = it }
            contactUpdateDto.contactInfos?.let { contactInfos ->
                huginTitle.contactInfos.clear()
                titleRepository.flush()
                contactInfos.forEach { ciDto ->
                    logger().info("Adding contact info: ${ciDto.contactType} -> ${ciDto.contactValue}")
                    huginTitle.contactInfos.add(
                        ContactInfo(
                            title = huginTitle,
                            contactType = ciDto.contactType,
                            contactValue = ciDto.contactValue,
                        )
                    )
                }
            }
            contactUpdateDto.releasePattern?.let { huginTitle.releasePattern = it.toTypedArray() }
            titleRepository.save(huginTitle)
        }

    @PostMapping("/newspapers/batch")
    @RolesAllowed("T_dimo_admin", "T_dimo_all")
    @Transactional
    suspend fun upsertNewspaper(
        @RequestBody upserts: List<NewspaperUpsertDto>,
        authentication: Authentication,
    ): List<Newspaper> = withContext(Dispatchers.IO) {
        val userName = (authentication.principal as? OidcUser)?.preferredUsername
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        upserts.map { upsert ->
            if (upsert.catalogId == null) {
                handleNewManifestation(upsert, userName).block()!!
            } else {
                newspaperService.getSingleManifestationAsItem(upsert.catalogId!!)
                    .onErrorResume { Mono.empty() }
                    .flatMap { existing -> handleExistingManifestation(upsert, existing, userName) }
                    .switchIfEmpty(Mono.defer { handleNewManifestation(upsert, userName) })
                    .block()!!
            }
        }
    }

    @DeleteMapping("/newspapers/{manifestationId}")
    @RolesAllowed("T_dimo_admin", "T_dimo_all")
    suspend fun deleteNewspaper(@PathVariable manifestationId: String): ResponseEntity<Void> =
        withContext(Dispatchers.IO) {
            newspaperService
                .deletePhysicalItemByManifestationId(manifestationId, true)
                .onErrorResume { Mono.empty() }
                .block()
            if (newspaperRepository.existsById(manifestationId)) {
                newspaperRepository.deleteById(manifestationId)
            }
            ResponseEntity.noContent().build()
        }

    // ── private helpers (copied verbatim from HuginNewspaperService) ──

    private fun handleExistingManifestation(
        upsert: NewspaperUpsertDto,
        existing: no.nb.bikube.api.core.model.Item,
        userName: String,
    ): Mono<Newspaper> {
        val existingNewspaper = newspaperRepository.findByIdOrNull(existing.parentCatalogueId!!)
            ?: upsert.catalogId?.let { newspaperRepository.findByIdOrNull(it) }
            ?: return Mono.just(saveNewspaperToDatabase(upsert.toNewspaper(existing.catalogueId)))
        return when {
            upsert.received == existingNewspaper.received ->
                newspaperService.updatePhysicalNewspaper(upsert.toItemUpdateDto(userName))
                    .thenReturn(saveNewspaperToDatabase(upsert.toNewspaper(existingNewspaper.catalogId)))
            !existingNewspaper.received!! ->
                newspaperService.createNewspaperItem(upsert.toItemInputDto(userName))
                    .map { saveNewspaperToDatabase(upsert.toNewspaper(existingNewspaper.catalogId)) }
            else ->
                newspaperService.deletePhysicalItemByManifestationId(existingNewspaper.catalogId, false)
                    .onErrorResume { Mono.empty() }
                    .thenReturn(saveNewspaperToDatabase(upsert.toNewspaper(existing.catalogueId)))
        }
    }

    private fun handleNewManifestation(upsert: NewspaperUpsertDto, userName: String): Mono<Newspaper> =
        if (upsert.received) {
            newspaperService.createNewspaperItem(upsert.toItemInputDto(userName)).map { it.parentCatalogueId!! }
        } else {
            newspaperService.createMissingItem(upsert.toMissingDto(userName)).map { it.catalogueId }
        }.map { catalogId -> saveNewspaperToDatabase(upsert.toNewspaper(catalogId)) }

    private fun saveNewspaperToDatabase(newspaper: Newspaper): Newspaper =
        newspaperRepository.save(newspaper)

    private fun NewspaperUpsertDto.toItemInputDto(username: String) = ItemInputDto(
        date = date, titleCatalogueId = titleId.toString(), digital = false,
        containerId = boxId, notes = notes, number = edition, username = username,
    )

    private fun NewspaperUpsertDto.toItemUpdateDto(username: String) = ItemUpdateDto(
        manifestationId = catalogId!!, username = username, notes = notes, number = edition,
    )

    private fun NewspaperUpsertDto.toNewspaper(catalogId: String): Newspaper {
        val box = boxRepository.findByIdOrNull(boxId) ?: error("Box $boxId not found")
        return Newspaper(
            catalogId = catalogId, box = box, date = date,
            edition = edition, received = received, notes = notes,
        )
    }

    private fun NewspaperUpsertDto.toMissingDto(username: String) = MissingPeriodicalItemDto(
        date = date, titleCatalogueId = titleId.toString(),
        username = username, notes = notes, number = edition,
    )
}
