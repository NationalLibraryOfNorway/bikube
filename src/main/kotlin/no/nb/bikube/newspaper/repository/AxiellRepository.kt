package no.nb.bikube.newspaper.repository

import no.nb.bikube.core.enum.*
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

    @Throws(AxiellCollectionsException::class)
    fun getAllTitles(): Mono<CollectionsModel> {
        return searchTexts(
            "record_type=${AxiellRecordType.WORK} and " +
            "work.description_type=${AxiellDescriptionType.SERIAL}"
        )
    }

    @Throws(AxiellCollectionsException::class)
    fun getAllItems(): Mono<CollectionsModel> {
        return searchTexts("record_type=${AxiellRecordType.ITEM}")
    }

    @Throws(AxiellCollectionsException::class)
    fun getSingleCollectionsModel(titleCatalogId: String): Mono<CollectionsModel> {
        return searchTexts("priref=${titleCatalogId}")
    }

    fun getTitleByName(name: String): Mono<CollectionsModel> {
        return searchTexts(
            "record_type=${AxiellRecordType.WORK} and " +
            "work.description_type=${AxiellDescriptionType.SERIAL} and " +
            "title=\"${name}\""
        )
    }

    fun searchPublisher(name: String): Mono<CollectionsModel> {
        return searchTexts(
            "name.type=${AxiellNameType.PUBLISHER} and name=\"${name}\"",
            AxiellDatabase.PEOPLE.value
        )
    }

    fun searchLanguage(name: String): Mono<CollectionsModel> {
        return searchTexts(
            "term.type=${AxiellTermType.LANGUAGE} and term=\"${name}\"",
            AxiellDatabase.LANGUAGES.value
        )
    }

    fun searchPublisherPlace(name: String): Mono<CollectionsModel> {
        return searchTexts("term=\"${name}\"", AxiellDatabase.LOCATIONS.value)
    }

    @Throws(AxiellCollectionsException::class)
    fun createRecord(
        serializedBody: String,
        database: String? = AxiellDatabase.TEXTS.value
    ): Mono<CollectionsModel> {
        return webClient()
            .post()
            .uri {
                it
                    .queryParam("database", database)
                    .queryParam("command", "insertrecord")
                    .queryParam("output", "json")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(serializedBody)
            .retrieve()
            .onStatus(
                { it.is4xxClientError || it.is5xxServerError },
                { Mono.error(AxiellCollectionsException("Error creating title")) }
            )
            .bodyToMono<CollectionsModel>()
    }

    @Throws(AxiellCollectionsException::class)
    private fun searchTexts(
        searchQuery: String,
        database: String? = AxiellDatabase.TEXTS.value
    ): Mono<CollectionsModel> {
        return webClient()
            .get()
            .uri {
                it
                    .queryParam("database", database)
                    .queryParam("output", "json")
                    .queryParam("search", searchQuery)
                    .build()
            }
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                {
                    logger().error(
                        "Could not search in Collections catalogue. Error code ${it.statusCode()}"
                    )
                    Mono.error(AxiellCollectionsException(
                        "Could not search in Collections catalogue. Try again later or contact Team Text if the problem persists."
                    ))
                }
            )
            .bodyToMono<CollectionsModel>()
    }

}
