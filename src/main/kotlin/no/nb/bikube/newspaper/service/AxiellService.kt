package no.nb.bikube.newspaper.service

import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericItem
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.core.model.CollectionsModel
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.config.AxiellConfig
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AxiellService  (
    axiellConfig: AxiellConfig
) {

    private val webClient = WebClient.builder().baseUrl(axiellConfig.url).build()

    @Throws(AxiellCollectionsException::class)
    fun getTitles(): Flux<Title> {
        return webClient
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
    fun getAllItems(): Flux<Item> {
        return webClient
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
}
