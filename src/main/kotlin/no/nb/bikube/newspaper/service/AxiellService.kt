package no.nb.bikube.newspaper.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericItem
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.core.mapper.mapCollectionsPartsObjectToGenericItem
import no.nb.bikube.core.model.*
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AxiellService  (
    private val axiellRepository: AxiellRepository
) {

    @Throws(AxiellCollectionsException::class)
    fun getTitles(): Flux<Title> {
        return axiellRepository.getAllTitles()
            .flatMapIterable { it.adlibJson.recordList }
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
            .flatMapIterable { it.adlibJson.recordList }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun getAllItems(): Flux<Item> {
        return axiellRepository.getAllItems()
            .flatMapIterable { it.adlibJson.recordList }
            .map { mapCollectionsObjectToGenericItem(it) }
    }

    @Throws(AxiellCollectionsException::class)
    fun getItemsForTitle(titleCatalogId: String): Flux<Item> {
        var titleName: String? = null
        var materialType: String? = null

        return axiellRepository.getItemsForTitle(titleCatalogId)
            .flatMapIterable { it.adlibJson.recordList }
            .flatMapIterable { title ->
                titleName = title.titleList?.first()?.title
                materialType = title.subMediumList?.first()?.subMedium

                if (
                    !title.partsList.isNullOrEmpty()
                ) { title.partsList }
                else { emptyList() }
            }
            .flatMapIterable { yearWork ->
                if (
                    yearWork.partsReference != null
                    && !yearWork.partsReference.partsList.isNullOrEmpty()
                ) { yearWork.partsReference.partsList }
                else { emptyList() }
            }
            .flatMapIterable { manifestation ->
                if (
                    manifestation.partsReference != null
                    && !manifestation.partsReference.partsList.isNullOrEmpty()
                ) { manifestation.partsReference.partsList }
                else { emptyList() }
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
