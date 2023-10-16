package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.mapper.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.collections.*
import no.nb.bikube.core.model.dto.*
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import reactor.kotlin.core.publisher.toMono
import java.time.LocalDate

@Service
class AxiellService  (
    private val axiellRepository: AxiellRepository
) {
    @Throws(AxiellCollectionsException::class)
    fun createNewspaperTitle(title: Title): Mono<Title> {
        val dto: TitleDto = createNewspaperTitleDto(title)
        val encodedBody = Json.encodeToString(dto)
        return axiellRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellTitleNotFound("New title not found"))
            }
            .map { mapCollectionsObjectToGenericTitle(it.first()) }
    }

    @Throws(AxiellCollectionsException::class)
    fun getItemsForTitle(titleCatalogId: String): Flux<Item> {

        return axiellRepository.getSingleCollectionsModel(titleCatalogId)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellTitleNotFound("Title $titleCatalogId not found"))
            }
            .map { it.first() }
            .handle { collectionsObject, sink ->
                if (collectionsObject.isSerial())
                    sink.next(collectionsObject)
                else
                    sink.error(AxiellTitleNotFound("Title $titleCatalogId not found"))
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

    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class)
    fun getSingleItem(catalogId: String): Mono<Item> {
        return axiellRepository.getSingleCollectionsModel(catalogId)
            .map {
                validateSingleCollectionsModel(it, AxiellRecordType.ITEM, null)
                mapCollectionsObjectToGenericItem(it.getFirstObject()!!)
            }
    }

    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class)
    fun getSingleTitle(catalogId: String): Mono<Title> {
        return axiellRepository.getSingleCollectionsModel(catalogId)
            .map {
                validateSingleCollectionsModel(it, AxiellRecordType.WORK, AxiellDescriptionType.SERIAL)
                mapCollectionsObjectToGenericTitle(it.getFirstObject()!!)
            }
    }

    fun searchTitleByName(name: String): Flux<CatalogueRecord> {
        return axiellRepository.getTitleByName(name)
            .flatMapIterable { it.getObjects() ?: emptyList() }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun createPublisher(publisher: String): Mono<Publisher> {
        if (publisher.isEmpty()) throw BadRequestBodyException("Publisher cannot be empty.")
        val serializedBody = Json.encodeToString(createNameRecordDtoFromString(publisher))
        return axiellRepository.searchPublisher(publisher)
            .flatMap { collectionsModel ->
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher '$publisher' already exists"))
                } else {
                    axiellRepository.createNameRecord(serializedBody, AxiellDatabase.PEOPLE)
                        .map { mapCollectionsObjectToGenericPublisher(it.getFirstObject()!!) }
                }
            }
    }

    fun createPublisherPlace(publisherPlace: String): Mono<PublisherPlace> {
        if (publisherPlace.isEmpty()) throw BadRequestBodyException("Publisher place cannot be empty.")
        val serializedBody = Json.encodeToString(createTermRecordDtoFromString(publisherPlace, AxiellTermType.LOCATION))
        return axiellRepository.searchPublisherPlace(publisherPlace)
            .flatMap { collectionsModel ->
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher place '$publisherPlace' already exists"))
                } else {
                    axiellRepository.createTermRecord(serializedBody, AxiellDatabase.LOCATIONS)
                        .map { mapCollectionsObjectToGenericPublisherPlace(it.getFirstObject()!!) }
                }
            }
    }

    fun createLanguage(language: String): Mono<Language> {
        if (!Regex("^[a-z]{3}$").matches(language)) {
            throw BadRequestBodyException("Language code must be a valid ISO-639-2 language code.")
        }
        val serializedBody = Json.encodeToString(createTermRecordDtoFromString(language, AxiellTermType.LANGUAGE))
        return axiellRepository.searchLanguage(language)
            .flatMap { collectionsModel ->
                if (collectionsModel.getObjects()?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Language '$language' already exists"))
                } else {
                    axiellRepository.createTermRecord(serializedBody, AxiellDatabase.LANGUAGES)
                        .map { mapCollectionsObjectToGenericLanguage(it.getFirstObject()!!) }
                }
            }
    }

    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class)
    private fun validateSingleCollectionsModel(model: CollectionsModel, recordType: AxiellRecordType?, workType: AxiellDescriptionType?) {
        val records = model.getObjects()
        if (records.isNullOrEmpty()) throw AxiellTitleNotFound("Could not find object in Collections")
        if (records.size > 1) throw AxiellCollectionsException("More than one object found in Collections (Should be exactly 1)")

        val record = records.first()
        recordType?.let {
            if (record.getRecordType() != recordType) {
                throw AxiellTitleNotFound("Could not find fitting object in Collections - Found object is not of type $recordType")
            }
        }
        workType?.let {
            if (record.getWorkType() != workType) {
                throw AxiellTitleNotFound("Could not find fitting object in Collections - Found object is not of type $workType")
            }
        }
    }

    fun createYearWork(titleCatalogueId: String, year: String): Mono<CollectionsObject> {
        val dto: YearDto = createYearDto(titleCatalogueId, year)
        val encodedBody = Json.encodeToString(dto)
        return axiellRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellYearWorkNotFound("New year not found"))
            }.map { it.first() }
    }

    fun createManifestation(titleCatalogueId: String, date: LocalDate): Mono<CollectionsObject> {
        val dto: ManifestationDto = createManifestationDto(titleCatalogueId, date)
        val encodedBody = Json.encodeToString(dto)
        return axiellRepository.createTextsRecord(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellManifestationNotFound("New manifestation not found"))
            }.map { it.first() }
    }

    @Throws(AxiellItemNotFound::class)
    fun createNewspaperItem(item: Item): Mono<Item> {
        return axiellRepository.getSingleCollectionsModel(item.titleCatalogueId!!)
            .flatMap {
                findOrCreateYearWorkRecord(it, item)
            }.flatMap { yearWork ->
                findOrCreateManifestationRecord(yearWork, item)
            }.flatMap {
                val dto: ItemDto = createNewspaperItemDto(item, it.partsReference?.priRef!!)
                val encodedBody = Json.encodeToString(dto)
                axiellRepository.createTextsRecord(encodedBody)
            }.handle { collectionsModel, sink ->
                collectionsModel.getObjects()
                    ?. let { sink.next(collectionsModel.getObjects()) }
                    ?: sink.error(AxiellItemNotFound("New item not found"))
            }
            .map { mapCollectionsObjectToGenericItem(it!!.first()) }
    }

    private fun findOrCreateManifestationRecord(
        yearWork: CollectionsPartsObject,
        item: Item
    ): Mono<CollectionsPartsObject> {
        return yearWork.getPartRefs().find { manifestation ->
            manifestation.partsReference?.dateStart == item.date?.toString()
        }?.toMono() ?: createManifestation(yearWork.partsReference!!.priRef!!, item.date!!)
            .map { collectionsObj ->
                mapCollectionsObjectToCollectionsPartObject(collectionsObj)
            }
    }

    private fun findOrCreateYearWorkRecord(
        title: CollectionsModel,
        item: Item
    ): Mono<CollectionsPartsObject> {
        return title.getFirstObject()?.getParts()?.find { year ->
            year.partsReference?.dateStart?.take(4) == item.date?.year.toString()
        }?.toMono() ?: createYearWork(item.titleCatalogueId!!, item.date?.year.toString())
            .map { collectionsObj ->
                mapCollectionsObjectToCollectionsPartObject(collectionsObj)
            }
    }

}
