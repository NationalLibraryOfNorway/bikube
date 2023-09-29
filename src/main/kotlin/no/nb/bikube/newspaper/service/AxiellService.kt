package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.exception.AxiellTitleNotFound
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericItem
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.core.mapper.mapCollectionsPartsObjectToGenericItem
import no.nb.bikube.core.model.*
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
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
    fun createTitle(title: Title): Flux<Title> {
        val encodedBody = Json.encodeToString(TitleDto(
            title = title.name!!,
            recordType = AxiellRecordType.WORK.value,
            descriptionType = AxiellDescriptionType.SERIAL.value,
            subMedium = title.materialType
        ))
        return axiellRepository.createTitle(encodedBody)
            .handle { collectionsModel, sink: SynchronousSink<List<CollectionsObject>> ->
                collectionsModel.adlibJson.recordList
                    ?. let { sink.next(collectionsModel.adlibJson.recordList) }
                    ?: sink.error(AxiellTitleNotFound("New title not found"))
            }
            .flatMapIterable { it }
            .map { mapCollectionsObjectToGenericTitle(it) }
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

}
