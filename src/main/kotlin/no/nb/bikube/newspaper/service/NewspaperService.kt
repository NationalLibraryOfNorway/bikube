package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.catalogue.collections.enum.*
import no.nb.bikube.catalogue.collections.exception.*
import no.nb.bikube.catalogue.collections.mapper.*
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.catalogue.collections.model.dto.*
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.TitleInputDto
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.util.function.Tuple2
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class NewspaperService  (
    private val collectionsRepository: CollectionsRepository
) {
    @Throws(CollectionsException::class)
    fun createNewspaperTitle(title: TitleInputDto): Mono<Title> {
        val dto: TitleDto = createNewspaperTitleDto(title)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(CollectionsTitleNotFound("New title not found"))
            }
            .flatMap { getSingleTitle(it.first().priRef) }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleItem(catalogId: String): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(catalogId)
            .map {
                validateSingleCollectionsModel(it, CollectionsRecordType.ITEM)
                mapCollectionsObjectToGenericItem(it.getFirstObject()!!)
            }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleTitle(catalogId: String): Mono<Title> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(catalogId)
            .map {
                validateSingleCollectionsModel(it, CollectionsRecordType.WORK)
                mapCollectionsObjectToGenericTitle(it.getFirstObject()!!)
            }
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
                    .map { itemPartReference ->
                        mapCollectionsPartsObjectToGenericItem(
                            itemPartReference.partsReference!!,
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
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher '$publisher' already exists"))
                } else {
                    collectionsRepository.createNameRecord(serializedBody, CollectionsDatabase.PEOPLE)
                        .map { mapCollectionsObjectToGenericPublisher(it.getFirstObject()!!) }
                }
            }
    }

    fun createPublisherPlace(
        publisherPlace: String,
        username: String
    ): Mono<PublisherPlace> {
        if (publisherPlace.isEmpty()) throw BadRequestBodyException("Publisher place cannot be empty.")
        return collectionsRepository.searchPublisherPlace(publisherPlace)
            .flatMap { collectionsModel ->
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher place '$publisherPlace' already exists"))
                } else {
                    val serializedBody = Json.encodeToString(createTermRecordDtoFromString(publisherPlace, CollectionsTermType.LOCATION, username))
                    collectionsRepository.createTermRecord(serializedBody, CollectionsDatabase.GEO_LOCATIONS)
                        .map { mapCollectionsObjectToGenericPublisherPlace(it.getFirstObject()!!) }
                }
            }
    }

    fun createLanguage(
        language: String,
        username: String
    ): Mono<Language> {
        if (!Regex("^[a-z]{3}$").matches(language)) {
            throw BadRequestBodyException("Language code must be a valid ISO-639-2 language code.")
        }
        val serializedBody = Json.encodeToString(createTermRecordDtoFromString(language, CollectionsTermType.LANGUAGE, username))
        return collectionsRepository.searchLanguage(language)
            .flatMap { collectionsModel ->
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Language '$language' already exists"))
                } else {
                    collectionsRepository.createTermRecord(serializedBody, CollectionsDatabase.LANGUAGES)
                        .map { mapCollectionsObjectToGenericLanguage(it.getFirstObject()!!) }
                }
            }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    private fun validateSingleCollectionsModel(
        model: CollectionsModel,
        recordType: CollectionsRecordType?
    ) {
        val records = model.getObjects()
        if (records.isNullOrEmpty()) throw CollectionsTitleNotFound("Could not find object in Collections")
        if (records.size > 1) throw CollectionsException("More than one object found in Collections (Should be exactly 1)")

        val record = records.first()
        recordType?.let {
            if (record.getRecordType() != recordType) {
                throw CollectionsTitleNotFound("Could not find fitting object in Collections - Found object is not of type $recordType")
            }
        }
    }

    fun createManifestation(
        titleCatalogueId: String,
        date: LocalDate,
        username: String
    ): Mono<CollectionsObject> {
        val dto: ManifestationDto = createManifestationDto(titleCatalogueId, date, username)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(CollectionsManifestationNotFound("New manifestation not found"))
            }.map { it.first() }
    }

    @Throws(CollectionsItemNotFound::class)
    fun createNewspaperItem(item: ItemInputDto): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModelWithoutChildren(item.titleCatalogueId)
            .flatMap { title ->
                if (title.hasError() || title.getFirstObject() == null) {
                    Mono.error(CollectionsItemNotFound("Title with id ${item.titleCatalogueId} not found: ${title.getError()}"))
                } else {
                    findOrCreateManifestationRecord(item)
                }
            }.flatMap { manifestation ->
                createLinkedNewspaperItem(item, manifestation.priRef)
            }
    }

    fun createTitleString(item: ItemInputDto, title: String): String {
        return if (item.name.isNullOrEmpty() && item.digital == true) {
            "$title ${item.date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
        } else {
            item.name!!
        }
    }

    private fun createLinkedNewspaperItem(
        item: ItemInputDto,
        parentId: String
    ): Mono<Item> {
        val dto: ItemDto = createNewspaperItemDto(item, parentId)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody).handle { collectionsModel, sink ->
            collectionsModel.getObjects()
                ?. let { sink.next(collectionsModel.getObjects()) }
                ?: sink.error(CollectionsItemNotFound("New item not found"))
        }.flatMap { getSingleItem(it!!.first().priRef) }
    }

    private fun findOrCreateManifestationRecord(
        item: ItemInputDto,
    ): Mono<CollectionsObject> {
        return collectionsRepository.getManifestationsByDateAndTitle(
            item.date, item.titleCatalogueId
        ).flatMap {
            if (it.isEmpty()) {
                createManifestation(item.titleCatalogueId, item.date, item.username)
            } else {
                Mono.just(it.getFirstObject()!!)
            }
        }
    }

    private fun filterByFormat(itemPartReference: CollectionsPartsObject?, isDigital: Boolean) =
        itemPartReference?.partsReference?.getFormat() == if (isDigital) CollectionsFormat.DIGITAL else CollectionsFormat.PHYSICAL
}
