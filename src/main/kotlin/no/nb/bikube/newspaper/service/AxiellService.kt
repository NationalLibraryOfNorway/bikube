package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.exception.AxiellTitleNotFound
import no.nb.bikube.core.exception.RecordAlreadyExistsException
import no.nb.bikube.core.mapper.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.dto.*
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink

@Service
class AxiellService  (
    private val axiellRepository: AxiellRepository
) {

    @Throws(AxiellCollectionsException::class)
    fun getTitles(): Flux<Title> {
        return axiellRepository.getAllTitles()
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    @Throws(AxiellCollectionsException::class)
    fun createTitle(title: Title): Mono<Title> {
        val encodedBody = Json.encodeToString(
            TitleDto(
                title = title.name!!,
                dateStart = title.startDate.toString(),
                dateEnd = title.endDate.toString(),
                publisher = title.publisher,
                placeOfPublication = title.publisherPlace,
                language = title.language,
                recordType = AxiellRecordType.WORK.value,
                descriptionType = AxiellDescriptionType.SERIAL.value,
                subMedium = title.materialType
            )
        )
        return axiellRepository.createRecord(encodedBody)
            .doOnNext{println(it)}
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellTitleNotFound("New title not found"))
            }
            .map { mapCollectionsObjectToGenericTitle(it.first()) }
    }

    fun getAllItems(): Flux<Item> {
        return axiellRepository.getAllItems()
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericItem(it) }
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
                val titleName = title.titleList?.first()?.title
                val materialType = title.subMediumList?.first()?.subMedium
                Flux.fromIterable(title.partsList ?: emptyList())
                    .flatMapIterable { yearWork ->
                        yearWork.partsReference?.partsList ?: emptyList()
                    }
                    .flatMapIterable { manifestation ->
                        manifestation.partsReference?.partsList ?: emptyList()
                    }
                    .mapNotNull { item ->
                        item.partsReference?.let {
                            mapCollectionsPartsObjectToGenericItem(
                                item.partsReference,
                                titleCatalogueId = titleCatalogId,
                                titleName = titleName,
                                materialType = materialType
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
                mapCollectionsObjectToGenericItem(it.adlibJson.recordList!!.first())
            }
    }

    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class)
    fun getSingleTitle(catalogId: String): Mono<Title> {
        return axiellRepository.getSingleCollectionsModel(catalogId)
            .map {
                validateSingleCollectionsModel(it, AxiellRecordType.WORK, AxiellDescriptionType.SERIAL)
                mapCollectionsObjectToGenericTitle(it.adlibJson.recordList!!.first())
            }
    }

    fun searchTitleByName(name: String): Flux<CatalogueRecord> {
        return axiellRepository.getTitleByName(name)
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun searchPublisherByName(name: String): Flux<CatalogueRecord> {
        return axiellRepository.searchPublisher(name)
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericPublisher(it) }
    }

    fun searchLanguageByName(name: String): Flux<CatalogueRecord> {
        return axiellRepository.searchLanguage(name)
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericLanguage(it) }
    }

    fun searchPublisherPlaceByName(name: String): Flux<CatalogueRecord> {
        return axiellRepository.searchPublisherPlace(name)
            .flatMapIterable { it.adlibJson.recordList ?: emptyList() }
            .map { mapCollectionsObjectToGenericPublisherPlace(it) }
    }

    fun createPublisher(publisher: String): Mono<Publisher> {
        val serializedBody = Json.encodeToString(createNameRecordDtoFromString(publisher))
        return axiellRepository.searchPublisher(publisher)
            .flatMap { collectionsModel ->
                if (collectionsModel.adlibJson.recordList?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher '$publisher' already exists"))
                } else {
                    axiellRepository.createRecord(serializedBody, AxiellDatabase.PEOPLE.value)
                        .map { mapCollectionsObjectToGenericPublisher(it.adlibJson.recordList!!.first()) }
                }
            }
    }

    fun createPublisherPlace(publisherPlace: String): Mono<PublisherPlace> {
        val serializedBody = Json.encodeToString(createTermRecordDtoFromString(publisherPlace, AxiellTermType.LOCATION))
        return axiellRepository.searchPublisherPlace(publisherPlace)
            .flatMap { collectionsModel ->
                if (collectionsModel.adlibJson.recordList?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Publisher place '$publisherPlace' already exists"))
                } else {
                    axiellRepository.createRecord(serializedBody, AxiellDatabase.LOCATIONS.value)
                        .map { mapCollectionsObjectToGenericPublisherPlace(it.adlibJson.recordList!!.first()) }
                }
            }
    }

    fun createLanguage(language: String): Mono<PublisherPlace> {
        val serializedBody = Json.encodeToString(createTermRecordDtoFromString(language, AxiellTermType.LANGUAGE))
        return axiellRepository.searchLanguage(language)
            .flatMap { collectionsModel ->
                if (collectionsModel.adlibJson.recordList?.isNotEmpty() == true) {
                    Mono.error(RecordAlreadyExistsException("Language '$language' already exists"))
                } else {
                    axiellRepository.createRecord(serializedBody, AxiellDatabase.LANGUAGES.value)
                        .map { mapCollectionsObjectToGenericPublisherPlace(it.adlibJson.recordList!!.first()) }
                }
            }
    }

    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class)
    private fun validateSingleCollectionsModel(model: CollectionsModel, recordType: AxiellRecordType?, workType: AxiellDescriptionType?) {
        val records = model.adlibJson.recordList
        if (records.isNullOrEmpty()) throw AxiellTitleNotFound("Could not find object in Collections")
        if (records.size > 1) throw AxiellCollectionsException("More than one object found in Collections (Should be exactly 1)")

        val record = records.first()
        recordType?.let {
            if (record.recordTypeList!!.first().first{ it.lang == "neutral" }.text != recordType.value) {
                throw AxiellTitleNotFound("Could not find fitting object in Collections - Found object is not of type $recordType")
            }
        }
        workType?.let {
            if (record.workTypeList!!.first().first{ it.lang == "neutral" }.text != workType.value) {
                throw AxiellTitleNotFound("Could not find fitting object in Collections - Found object is not of type $workType")
            }
        }
    }

}
