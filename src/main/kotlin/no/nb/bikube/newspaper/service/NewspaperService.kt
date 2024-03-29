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
import reactor.kotlin.core.publisher.toMono
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

    @Throws(CollectionsException::class)
    fun getItemsForTitle(titleCatalogId: String): Flux<Item> {

        return collectionsRepository.getSingleCollectionsModel(titleCatalogId)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(CollectionsTitleNotFound("Title $titleCatalogId not found"))
            }
            .map { it.first() }
            .handle { collectionsObject, sink ->
                if (collectionsObject.isSerial())
                    sink.next(collectionsObject)
                else
                    sink.error(CollectionsTitleNotFound("Title $titleCatalogId not found"))
            }
            .flatMapMany { title ->
                val titleName = title.getName()
                val materialType = title.getMaterialType()
                Flux.fromIterable(title.partsList ?: emptyList())
                    .flatMapIterable { yearWork ->
                        yearWork.getPartRefs()
                    }
                    .flatMapIterable { manifestation ->
                        manifestation.getPartRefs()
                    }
                    .mapNotNull { item ->
                        item.partsReference?.let {
                            mapCollectionsPartsObjectToGenericItem(
                                item.partsReference,
                                titleCatalogueId = titleCatalogId,
                                titleName = titleName,
                                materialType = materialType?.norwegian
                            )
                        }
                    }
            }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleItem(catalogId: String): Mono<Item> {
        return collectionsRepository.getSingleCollectionsModel(catalogId)
            .map {
                validateSingleCollectionsModel(it, CollectionsRecordType.ITEM, null)
                mapCollectionsObjectToGenericItem(it.getFirstObject()!!)
            }
    }

    @Throws(CollectionsException::class, CollectionsTitleNotFound::class)
    fun getSingleTitle(catalogId: String): Mono<Title> {
        return collectionsRepository.getSingleCollectionsModel(catalogId)
            .map {
                validateSingleCollectionsModel(it, CollectionsRecordType.WORK, CollectionsDescriptionType.SERIAL)
                mapCollectionsObjectToGenericTitle(it.getFirstObject()!!)
            }
    }

    fun searchTitleByName(name: String): Flux<CatalogueRecord> {
        return collectionsRepository.getTitleByName(name)
            .flatMapIterable { it.getObjects() ?: emptyList() }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun getItemsByTitle(
        titleCatalogId: String,
        date: LocalDate,
        isDigital: Boolean,
        materialType: MaterialType
    ): Flux<CatalogueRecord> {
        return collectionsRepository.getWorkYearForTitle(titleCatalogId, date.year)
            .flatMapIterable { it.getObjects() ?: emptyList() }
            .flatMap { titleObject ->
                val title = titleObject.getName() ?: ""
                Flux.fromIterable(titleObject.getParts() ?: emptyList())
                    .filter { manifestation -> filterByDate(manifestation, date) }
                    .flatMap { manifestation -> collectionsRepository.getSingleCollectionsModel(manifestation.partsReference?.priRef!!) }
                    .flatMapIterable { it.getObjects() ?: emptyList() }
                    .flatMapIterable { it.getParts() ?: emptyList() }
                    .filter { itemPartReference -> filterByFormat(itemPartReference, isDigital) }
                    .map { itemPartReference ->
                        mapCollectionsPartsObjectToGenericItem(
                            itemPartReference.partsReference!!,
                            titleCatalogId,
                            title,
                            materialType.value,
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
    private fun validateSingleCollectionsModel(model: CollectionsModel, recordType: CollectionsRecordType?, workType: CollectionsDescriptionType?) {
        val records = model.getObjects()
        if (records.isNullOrEmpty()) throw CollectionsTitleNotFound("Could not find object in Collections")
        if (records.size > 1) throw CollectionsException("More than one object found in Collections (Should be exactly 1)")

        val record = records.first()
        recordType?.let {
            if (record.getRecordType() != recordType) {
                throw CollectionsTitleNotFound("Could not find fitting object in Collections - Found object is not of type $recordType")
            }
        }
        workType?.let {
            if (record.getWorkType() != workType) {
                throw CollectionsTitleNotFound("Could not find fitting object in Collections - Found object is not of type $workType")
            }
        }
    }

    fun createYearWork(
        titleCatalogueId: String,
        year: String,
        username: String
    ): Mono<CollectionsObject> {
        val dto: YearDto = createYearDto(titleCatalogueId, year, username)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(CollectionsYearWorkNotFound("New year not found"))
            }.map { it.first() }
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
        return collectionsRepository.getSingleCollectionsModel(item.titleCatalogueId)
            .flatMap { title ->
                item.name = createTitleString(item, title.getFirstObject()?.getName()!!)
                findOrCreateYearWorkRecord(title, item)
            }.flatMap { yearWork ->
                findOrCreateManifestationRecord(yearWork, item)
            }.flatMap { manifestation ->
                createLinkedNewspaperItem(item, manifestation)
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
        manifestation: CollectionsPartsObject
    ): Mono<Item> {
        val dto: ItemDto = createNewspaperItemDto(item, manifestation.partsReference?.priRef!!)
        val encodedBody = Json.encodeToString(dto)
        return collectionsRepository.createTextsRecord(encodedBody).handle { collectionsModel, sink ->
            collectionsModel.getObjects()
                ?. let { sink.next(collectionsModel.getObjects()) }
                ?: sink.error(CollectionsItemNotFound("New item not found"))
        }.flatMap { getSingleItem(it!!.first().priRef) }
    }

    private fun findOrCreateManifestationRecord(
        yearWork: CollectionsPartsObject,
        item: ItemInputDto
    ): Mono<CollectionsPartsObject> {
        return yearWork.getPartRefs().find { manifestation ->
            manifestation.getDate() == item.date
        }?.toMono() ?: createManifestation(yearWork.partsReference!!.priRef!!, item.date, item.username)
            .map { collectionsObj ->
                mapCollectionsObjectToCollectionsPartObject(collectionsObj)
            }
    }

    private fun findOrCreateYearWorkRecord(
        title: CollectionsModel,
        item: ItemInputDto
    ): Mono<CollectionsPartsObject> {
        return title.getFirstObject()?.getParts()?.find { year ->
            year.getDate()?.year == item.date.year
        }?.toMono() ?: createYearWork(item.titleCatalogueId, item.date.year.toString(), item.username)
            .map { collectionsObj ->
                mapCollectionsObjectToCollectionsPartObject(collectionsObj)
            }
    }

    private fun filterByFormat(itemPartReference: CollectionsPartsObject?, isDigital: Boolean) =
        itemPartReference?.partsReference?.getFormat() == if (isDigital) CollectionsFormat.DIGITAL else CollectionsFormat.PHYSICAL

    private fun filterByDate(manifestationPartsObject: CollectionsPartsObject, date: LocalDate) =
        manifestationPartsObject.getDate().toString() == date.toString()

}
