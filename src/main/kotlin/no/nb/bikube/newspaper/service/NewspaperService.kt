package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.catalogue.collections.enum.*
import no.nb.bikube.catalogue.collections.exception.*
import no.nb.bikube.catalogue.collections.mapper.*
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.catalogue.collections.model.dto.*
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.catalogue.collections.service.CollectionsLocationService
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.core.model.inputDto.TitleInputDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class NewspaperService  (
    private val collectionsRepository: CollectionsRepository,
    private val collectionsLocationService: CollectionsLocationService,
    private val uniqueIdService: UniqueIdService
) {

    @Throws(CollectionsException::class)
    fun createNewspaperTitle(title: TitleInputDto): Mono<Title> {
        val id = uniqueIdService.getUniqueId()
        val dto: TitleDto = createNewspaperTitleDto(id, title)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsTitleNotFound("New title not found"))
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
        return collectionsRepository.getManifestationsByDateAndTitle(date, titleCatalogId)
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
    ): Mono<CollectionsObject> {
        val id = uniqueIdService.getUniqueId()
        val dto: ManifestationDto = createManifestationDto(id, titleCatalogueId, date, username, notes)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsManifestationNotFound("New manifestation not found"))
            }
    }

    @Throws(CollectionsItemNotFound::class)
    fun createNewspaperItem(item: ItemInputDto): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(item.titleCatalogueId)
            .flatMap { title ->
                if (title.hasError() || !title.hasObjects()) {
                    Mono.error(CollectionsItemNotFound("Title with id ${item.titleCatalogueId} not found: ${title.getError()}"))
                } else {
                    findOrCreateManifestationRecord(item.titleCatalogueId, item.date, item.username, item.notes)
                }
            }.flatMap { manifestation ->
                if (item.digital == false && !item.containerId.isNullOrBlank()) {
                    collectionsLocationService.createContainerIfNotExists(item.containerId, item.username)
                        .then(createLinkedNewspaperItem(item, manifestation.priRef))
                } else {
                    createLinkedNewspaperItem(item, manifestation.priRef)
                }
            }
    }

    fun createTitleString(item: ItemInputDto, title: String): String? {
        return if (item.name.isNullOrEmpty() && item.digital == true) {
            "$title ${item.date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
        } else {
            item.name
        }
    }

    fun createMissingItem(item: MissingPeriodicalItemDto): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(item.titleCatalogueId)
            .flatMap { title ->
                if (title.hasError() || !title.hasObjects()) {
                    Mono.error(CollectionsItemNotFound("Title with id ${item.titleCatalogueId} not found: ${title.getError()}"))
                } else {
                    findOrCreateManifestationRecord(item.titleCatalogueId, item.date, item.username, item.notes)
                }
            }.flatMap { getSingleManifestationAsItem(it.priRef) }
    }

    private fun createLinkedNewspaperItem(
        item: ItemInputDto,
        parentId: String
    ): Mono<Item> {
        val uniqueId = uniqueIdService.getUniqueId()
        val dto: ItemDto = createNewspaperItemDto(uniqueId, item, parentId)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink ->
                if (collectionsModel.hasObjects())
                    sink.next(collectionsModel.getFirstObject())
                else
                    sink.error(CollectionsItemNotFound("New item not found"))
            }
            .flatMap { getSingleItem(it.priRef) }
    }

    private fun findOrCreateManifestationRecord(
        titleId: String,
        date: LocalDate,
        username: String,
        notes: String?
    ): Mono<CollectionsObject> {
        return collectionsRepository.getManifestationsByDateAndTitle(
            date, titleId
        ).flatMap {
            if (!it.hasObjects()) {
                createManifestation(titleId, date, username, notes)
            } else {
                Mono.just(it.getFirstObject())
            }
        }
    }

    private fun filterByFormat(itemPartReference: CollectionsPartsObject?, isDigital: Boolean) =
        itemPartReference?.partsReference?.getFormat() == if (isDigital) CollectionsFormat.DIGITAL else CollectionsFormat.PHYSICAL
}
