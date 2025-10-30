package no.nb.bikube.api.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.api.catalogue.collections.config.CollectionsConfig
import no.nb.bikube.api.catalogue.collections.enum.*
import no.nb.bikube.api.catalogue.collections.exception.*
import no.nb.bikube.api.catalogue.collections.mapper.*
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.model.dto.*
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.api.catalogue.collections.service.CollectionsLocationService
import no.nb.bikube.api.core.enum.*
import no.nb.bikube.api.core.exception.*
import no.nb.bikube.api.core.model.*
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.api.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.api.core.model.inputDto.TitleInputDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.net.URL
import java.time.LocalDate

@Service
class NewspaperService (
    private val collectionsConfig: CollectionsConfig,
    private val collectionsRepository: CollectionsRepository,
    private val collectionsLocationService: CollectionsLocationService,
    private val uniqueIdService: UniqueIdService
) {

    @Throws(CollectionsException::class)
    fun createNewspaperTitle(title: TitleInputDto): Mono<Title> {
        val id = uniqueIdService.getUniqueId()
        val dto: TitleDto = createNewspaperTitleDto(id, title)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createNewspaperRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsException("Error creating title: ${collectionsModel.getError()}"))
            }
            .flatMap { getSingleTitle(it.priRef) }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleItem(catalogId: String): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(catalogId)
            .map { validateAndReturnSingleCollectionsModel(it, CollectionsRecordType.ITEM) }
            .map { mapCollectionsObjectToGenericItem(it) }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleManifestationAsItem(catalogId: String): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModel(catalogId)
            .map { validateAndReturnSingleCollectionsModel(it, CollectionsRecordType.MANIFESTATION) }
            .map { mapCollectionsObjectToGenericItem(it) }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleTitle(catalogId: String): Mono<Title> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(catalogId)
            .map { validateAndReturnSingleCollectionsModel(it, CollectionsRecordType.WORK) }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun getLinkToSingleTitle(catalogId: String): URL {
        return collectionsConfig.linkTemplate
            .build(catalogId)
            .toURL()
    }

    fun getTitlesPage(pageNumber: Int): Mono<Tuple2<List<Title>, Int>> {
        val pageContent = collectionsRepository.getAllNewspaperTitles(pageNumber)
            .mapNotNull { model ->
                model.getObjects()
                    ?. map { mapCollectionsObjectToGenericTitle(it) }
            }
        return Mono.zip(pageContent, Mono.just(pageNumber))
    }

    fun getAllTitles(): Mono<List<Title>> {
        return getTitlesPage(1)
            .expand { p -> getTitlesPage(p.t2 + 1) }
            .flatMapIterable { it.t1 }
            .collectList()
    }

    fun getItemsByTitleAndDate(
        titleCatalogId: String,
        date: LocalDate,
        isDigital: Boolean?
    ): Flux<CatalogueRecord> {
        return collectionsRepository.getManifestations(date, titleCatalogId)
            .flatMapIterable { it.getObjects() ?: emptyList() }
            .flatMap { briefManifestation ->
                collectionsRepository.getSingleCollectionsModel(briefManifestation.priRef)
                    .flatMapIterable { it.getObjects() ?: emptyList() }
                    .flatMapIterable { it.getParts() ?: emptyList() }
                    .filter { if (isDigital != null) filterByFormat(it, isDigital) else true }
                    .mapNotNull { it.partsReference }
                    .map { partsReference ->
                        mapCollectionsPartsObjectToGenericItem(
                            partsReference!!,  // null values filtered in mapNotNull
                            titleCatalogId,
                            briefManifestation.getName() ?: "",
                            MaterialType.NEWSPAPER.value,
                            date.toString()
                        )
                    }
            }
    }

    fun createPublisher(
        publisher: String,
        username: String
    ): Mono<Publisher> {
        if (publisher.isEmpty()) throw BadRequestBodyException("Publisher cannot be empty.")
        val serializedBody = Json.encodeToString(createNameRecordDtoFromString(publisher, username))
        return collectionsRepository.searchPublisher(publisher)
            .flatMap { collectionsModel ->
                if (collectionsModel.hasObjects()) {
                    Mono.error(RecordAlreadyExistsException("Publisher '$publisher' already exists"))
                } else {
                    collectionsRepository.createNameRecord(serializedBody, CollectionsDatabase.PEOPLE)
                        .map { mapCollectionsObjectToGenericPublisher(it.getFirstObject()) }
                }
            }
    }

    fun createPublisherPlace(
        publisherPlace: String,
        username: String
    ): Mono<PublisherPlace> {
        if (publisherPlace.isEmpty()) throw BadRequestBodyException("Publisher place cannot be empty.")
        return collectionsRepository.searchPublisherPlace(publisherPlace)
            .handle { collectionsTermModel, synchronousSink ->
                if (collectionsTermModel.hasObjects())
                    synchronousSink.error(RecordAlreadyExistsException("Publisher place '$publisherPlace' already exists"))
                else
                    synchronousSink.next(
                        Json.encodeToString(
                            createTermRecordDtoFromString(
                                publisherPlace,
                                CollectionsTermType.LOCATION,
                                username
                            )
                        )
                    )
            }
            .flatMap { serializedBody ->
                collectionsRepository.createTermRecord(serializedBody, CollectionsDatabase.GEO_LOCATIONS)
                    .map { mapCollectionsObjectToGenericPublisherPlace(it.getFirstObject()) }
            }
    }

    fun createLanguage(
        language: String,
        username: String
    ): Mono<Language> {
        if (!Regex("^[a-z]{3}$").matches(language)) {
            throw BadRequestBodyException("Language code must be a valid ISO-639-2 language code.")
        }
        return collectionsRepository.searchLanguage(language)
            .handle { collectionsTermModel, synchronousSink ->
                if (collectionsTermModel.hasObjects())
                    synchronousSink.error(RecordAlreadyExistsException("Language '$language' already exists"))
                else
                    synchronousSink.next(
                        Json.encodeToString(
                            createTermRecordDtoFromString(
                                language,
                                CollectionsTermType.LANGUAGE,
                                username
                            )
                        )
                    )
            }
            .flatMap { serializedBody ->
                collectionsRepository.createTermRecord(serializedBody, CollectionsDatabase.LANGUAGES)
                    .map { mapCollectionsObjectToGenericLanguage(it.getFirstObject()) }
            }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    private fun validateAndReturnSingleCollectionsModel(
        model: CollectionsModel,
        recordType: CollectionsRecordType?
    ): CollectionsObject {
        val records = model.getObjects()
        if (records.isNullOrEmpty()) throw CollectionsTitleNotFound("Could not find object in Collections")
        if (records.size > 1) throw CollectionsException("More than one object found in Collections (Should be exactly 1)")

        val record = records.first()
        recordType?.let {
            if (record.getRecordType() != recordType) {
                throw CollectionsTitleNotFound("Could not find fitting object in Collections - Found object is not of type $recordType")
            }
        }
        return record
    }

    fun createManifestation(
        titleCatalogueId: String,
        date: LocalDate,
        username: String,
        notes: String?,
        number: String?
    ): Mono<CollectionsObject> {
        val id = uniqueIdService.getUniqueId()
        val dto: ManifestationDto = createManifestationDto(id, titleCatalogueId, date, username, notes, number)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createNewspaperRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsException("Error creating manifestation: ${collectionsModel.getError()}"))
            }
    }

    @Throws(CollectionsItemNotFound::class)
    fun createNewspaperItem(item: ItemInputDto): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(item.titleCatalogueId)
            .flatMap { title ->
                if (title.hasError() || !title.hasObjects()) {
                    Mono.error(CollectionsItemNotFound("Title with id ${item.titleCatalogueId} not found: ${title.getError()}"))
                } else {
                    findOrCreateManifestationRecord(item.titleCatalogueId, item.date, item.username, item.notes, item.number)
                }
            }.flatMap { manifestationReference ->
                collectionsRepository.getSingleCollectionsModel(manifestationReference.priRef)
            }
            .flatMap { manifestation ->
                val manifestationId = manifestation.getFirstObject().priRef
                checkForExistingItems(manifestation.getFirstObject(), item).then(
                    if (item.digital == true) {
                        createLinkedNewspaperItem(item, manifestationId)
                    } else if (!item.containerId.isNullOrBlank()) {
                        collectionsLocationService.createContainerIfNotExists(item.containerId, item.username)
                            .then(createLinkedNewspaperItem(item, manifestationId))
                    } else {
                        Mono.error(CollectionsPhysicalItemMissingContainer("Physical item must have a container ID"))
                    }
                )
            }
    }

    fun createMissingItem(item: MissingPeriodicalItemDto): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(item.titleCatalogueId)
            .flatMap { title ->
                if (title.hasError() || !title.hasObjects()) {
                    Mono.error(CollectionsItemNotFound("Title with id ${item.titleCatalogueId} not found: ${title.getError()}"))
                } else {
                    findOrCreateManifestationRecord(item.titleCatalogueId, item.date, item.username, item.notes, item.number)
                }
            }.flatMap { getSingleManifestationAsItem(it.priRef) }
    }

    fun updatePhysicalNewspaper(updateDto: ItemUpdateDto): Mono<CollectionsObject> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(updateDto.manifestationId)
            .flatMap { manifestation ->
                if (manifestation.hasError()) {
                    Mono.error(CollectionsException("Error when updating manifestation: ${manifestation.getError()}"))
                } else if (!manifestation.hasObjects()) {
                    Mono.error(CollectionsManifestationNotFound("Manifestation with id ${updateDto.manifestationId} not found."))
                } else if (manifestation.getFirstObject().getRecordType() == CollectionsRecordType.MANIFESTATION) {
                    updateManifestation(updateDto)
                } else {
                    Mono.error(NotSupportedException("Only manifestations can be edited this way. Catalog item with id ${updateDto.manifestationId} is not a manifestation"))
                }
            }
    }

    fun deletePhysicalItemByManifestationId(manifestationId: String, deleteManifestation: Boolean): Mono<CollectionsModel> {
        return collectionsRepository.getSingleCollectionsModel(manifestationId)
            .flatMap { manifestation ->
                if (manifestation.hasObjects() && manifestation.getFirstObject().getRecordType() == CollectionsRecordType.MANIFESTATION) {
                    deleteItemAndManifestationIfNoOtherItems(manifestation.getFirstObject(), deleteManifestation)
                } else if (manifestation.hasObjects() && manifestation.getFirstObject().getRecordType() != CollectionsRecordType.MANIFESTATION) {
                    Mono.error(NotSupportedException("Catalog item with id $manifestationId is not a manifestation. Must be a manifestation to delete this way."))
                } else if (!manifestation.hasObjects()) {
                    Mono.error(CollectionsManifestationNotFound("Manifestation with id $manifestationId not found."))
                } else {
                    Mono.error(CollectionsException("Error when finding manifestation for deletion: ${manifestation.getError()}"))
                }
            }
    }

    private fun checkForExistingItems(
        manifestation: CollectionsObject,
        item: ItemInputDto
    ): Mono<Void> {
        val allItems = manifestation.getParts() ?: emptyList()
        val hasDigitalItem = allItems.any { it.partsReference?.getFormat() == CollectionsFormat.DIGITAL }
        val hasPhysicalItem = allItems.any { it.partsReference?.getFormat() == CollectionsFormat.PHYSICAL }
        return when {
            hasDigitalItem && item.digital == true -> Mono.error(CollectionsManifestationItemsAlreadyExist("Manifestation already has a digital item"))
            hasPhysicalItem && item.digital == false -> Mono.error(CollectionsManifestationItemsAlreadyExist("Manifestation already has a physical item"))
            else -> Mono.empty()
        }
    }

    private fun deleteItemAndManifestationIfNoOtherItems(
        manifestation: CollectionsObject,
        deleteManifestation: Boolean
    ): Mono<CollectionsModel> {
        val allItems = manifestation.getParts()
        val physicalItems = allItems?.filter {
            it.partsReference?.getFormat() == CollectionsFormat.PHYSICAL
        }

        val firstPhysicalItem = physicalItems?.firstOrNull()

        return if (physicalItems?.size == 1 && firstPhysicalItem?.partsReference != null) {
            if (allItems.size == 1 && deleteManifestation) {
                collectionsRepository.deleteNewspaperRecord(firstPhysicalItem.partsReference.priRef)
                    .then(collectionsRepository.deleteNewspaperRecord(manifestation.priRef))
            } else {
                collectionsRepository.deleteNewspaperRecord(firstPhysicalItem.partsReference.priRef)
            }
        } else if (allItems.isNullOrEmpty() && deleteManifestation) {
            collectionsRepository.deleteNewspaperRecord(manifestation.priRef)
        } else if (allItems.isNullOrEmpty()) {
            Mono.error(CollectionsException("Manifestation had no items, expected at least 1"))
        } else {
            Mono.error(CollectionsException("Manifestation had ${physicalItems?.size ?: 0} physical items, expected 1"))
        }
    }

    private fun createLinkedNewspaperItem(
        item: ItemInputDto,
        parentId: String
    ): Mono<Item> {
        val uniqueId = uniqueIdService.getUniqueId()
        val dto: ItemDto = createNewspaperItemDto(uniqueId, item, parentId)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createNewspaperRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsException("Error creating item: ${collectionsModel.getError()}"))
            }
            .flatMap { getSingleItem(it.priRef) }
    }

    private fun findOrCreateManifestationRecord(
        titleId: String,
        date: LocalDate,
        username: String,
        notes: String?,
        number: String?
    ): Mono<CollectionsObject> {
        return collectionsRepository.getManifestations(
            date, titleId, number
        ).flatMap {
            if (!it.hasObjects()) {
                createManifestation(titleId, date, username, notes, number)
            } else {
                Mono.just(it.getFirstObject())
            }
        }
    }

    private fun updateManifestation(
        item: ItemUpdateDto
    ): Mono<CollectionsObject> {
        val dto = createUpdateManifestationDto(item.manifestationId, item.username, item.notes, item.number)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.updateNewspaperRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsException("Error when updating manifestation: ${collectionsModel.getError()}"))
            }
    }

    private fun filterByFormat(itemPartReference: CollectionsPartsObject?, isDigital: Boolean) =
        itemPartReference?.partsReference?.getFormat() == if (isDigital) CollectionsFormat.DIGITAL else CollectionsFormat.PHYSICAL
}
