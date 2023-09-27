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
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.config.WebClientConfig
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AxiellService  (
    private val webClient: WebClientConfig
) {

    @Throws(AxiellCollectionsException::class)
    fun getTitles(): Flux<Title> {
        return webClient.webClient()
            .get()
            .uri {
                it
                    .queryParam("database", "texts")
                    .queryParam(
                        "search", "" +
                            "record_type=${AxiellRecordType.WORK} and " +
                            "work.description_type=${AxiellDescriptionType.SERIAL}"
                    )
                    .queryParam("output", "json")
                    .build()
            }
            .retrieve()
            .onStatus(
                {!it.is2xxSuccessful},
                {
                    logger().error("Could not get titles from Collections. Error code ${it.statusCode()}")
                    Mono.error(AxiellCollectionsException("Could not get newspaper titles from Collections."))
                }
            )
            .bodyToMono<CollectionsModel>()
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
        return webClient.webClient()
            .post()
            .uri {
                it
                    .queryParam("database", "texts")
                    .queryParam("command", "insertrecord")
                    .queryParam("output", "json")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(encodedBody)
            .retrieve()
            .onStatus(
                { it.is4xxClientError || it.is5xxServerError },
                { Mono.error(RuntimeException("Error creating title")) }
            )
            .bodyToMono<CollectionsModel>()
            .flatMapIterable { it.adlibJson.recordList }
            .map { mapCollectionsObjectToGenericTitle(it) }
    }

    fun getAllItems(): Flux<Item> {
        return webClient.webClient()
            .get()
            .uri {
                it
                    .queryParam("database", "texts")
                    .queryParam("search", "record_type=${AxiellRecordType.ITEM}")
                    .queryParam("output", "json")
                    .build()
            }
            .retrieve()
            .onStatus(
                {!it.is2xxSuccessful},
                {
                    logger().error("Could not get titles from Collections. Error code ${it.statusCode()}")
                    Mono.error(AxiellCollectionsException("Could not get newspaper items from Collections."))
                }
            )
            .bodyToMono<CollectionsModel>()
            .flatMapIterable { it.adlibJson.recordList }
            .map { mapCollectionsObjectToGenericItem(it) }
    }

    @Throws(AxiellCollectionsException::class)
    fun getSingleCollectionsModel(catalogId: String): Mono<CollectionsModel> {
        return webClient.webClient()
            .get()
            .uri {
                it
                    .queryParam("search", "priref=$catalogId")
                    .queryParam("database", "texts")
                    .queryParam("output", "json")
                    .build()
            }
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                {
                    logger().error("Could not get titles from Collections. Error code ${it.statusCode()}")
                    Mono.error(AxiellCollectionsException("Could not get newspaper items from Collections."))
                }
            )
            .bodyToMono<CollectionsModel>()
    }

    @Throws(AxiellCollectionsException::class)
    fun getItemsForTitle(titleCatalogId: String): Flux<Item> {
        var titleName: String? = null
        var titleId: String? = null

        return getSingleCollectionsModel(titleCatalogId)
            .flatMapIterable { it.adlibJson.recordList }
            .flatMapIterable { title ->
                titleName = title.titleList?.first()?.title
                titleId = title.priRef

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
                    mapCollectionsPartsObjectToGenericItem(item.partsReference, titleCatalogueId = titleId, titleName = titleName)
                }
            }
    }
}
