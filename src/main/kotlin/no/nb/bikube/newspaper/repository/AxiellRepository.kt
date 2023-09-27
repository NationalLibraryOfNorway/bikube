package no.nb.bikube.newspaper.repository

import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.CollectionsModel
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.config.WebClientConfig
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Repository
class AxiellRepository(
    private val webClientConfig: WebClientConfig
) {
    fun webClient() = webClientConfig.webClient()

    fun getTitles(): Mono<CollectionsModel> {
        return webClient()
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
    }

    fun createTitle(serializedBody: String): Mono<CollectionsModel> {
        return webClient()
            .post()
            .uri {
                it
                    .queryParam("database", "texts")
                    .queryParam("command", "insertrecord")
                    .queryParam("output", "json")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(serializedBody)
            .retrieve()
            .onStatus(
                { it.is4xxClientError || it.is5xxServerError },
                { Mono.error(RuntimeException("Error creating title")) }
            )
            .bodyToMono<CollectionsModel>()
    }

    fun getAllItems(): Mono<CollectionsModel> {
        return webClient()
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
    }

    fun getSingleCollectionsModel(catalogId: String): Mono<CollectionsModel> {
        return webClient()
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


}