package no.nb.bikube.newspaper.repository

import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.collections.CollectionsModel
import no.nb.bikube.core.model.collections.CollectionsNameModel
import no.nb.bikube.core.model.collections.CollectionsTermModel
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.config.CollectionsWebClient
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Repository
class AxiellRepository(
    private val collectionsWebClient: CollectionsWebClient
) {
    fun webClient() = collectionsWebClient.webClient()

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

    fun searchPublisher(name: String): Mono<CollectionsNameModel> {
        return searchNameDatabases("name.type=${AxiellNameType.PUBLISHER} and name=\"${name}\"")
    }

    fun searchLanguage(name: String): Mono<CollectionsTermModel> {
        return searchTermDatabases(
            "term.type=${AxiellTermType.LANGUAGE} and term=\"${name}\"",
            AxiellDatabase.LANGUAGES
        )
    }

    fun searchPublisherPlace(name: String): Mono<CollectionsTermModel> {
        return searchTermDatabases("term=\"${name}\"", AxiellDatabase.LOCATIONS)
    }

    @Throws(AxiellCollectionsException::class)
    fun createTextsRecord(serializedBody: String): Mono<CollectionsModel> {
        return createRecordWebClientRequest(serializedBody, AxiellDatabase.TEXTS).bodyToMono<CollectionsModel>()
    }

    fun createNameRecord(serializedBody: String, db: AxiellDatabase): Mono<CollectionsNameModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsNameModel>()
    }

    fun createTermRecord(serializedBody: String, db: AxiellDatabase): Mono<CollectionsTermModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsTermModel>()
    }

    private fun searchTexts(query: String): Mono<CollectionsModel> {
        return getRecordsWebClientRequest(query, AxiellDatabase.TEXTS).bodyToMono<CollectionsModel>()
    }

    private fun searchNameDatabases(query: String): Mono<CollectionsNameModel> {
        return getRecordsWebClientRequest(query, AxiellDatabase.PEOPLE).bodyToMono<CollectionsNameModel>()
    }

    private fun searchTermDatabases(query: String, db: AxiellDatabase): Mono<CollectionsTermModel> {
        return getRecordsWebClientRequest(query, db).bodyToMono<CollectionsTermModel>()
    }

    private fun getRecordsWebClientRequest(query: String, db: AxiellDatabase): WebClient.ResponseSpec {
        return webClient()
            .get()
            .uri {
                it
                    .queryParam("database", db.value)
                    .queryParam("output", "json")
                    .queryParam("search", query)
                    .build()
            }
            .retrieve()
            .onStatus(
                { !it.is2xxSuccessful },
                {
                    logger().error(
                        "Could not search in Collections ${db.value} catalogue. Error code ${it.statusCode()}"
                    )
                    Mono.error(AxiellCollectionsException(
                        "Could not search in Collections catalogue. Try again later or contact Team Text if the problem persists."
                    ))
                }
            )
    }

    private fun createRecordWebClientRequest(serializedBody: String, db: AxiellDatabase): WebClient.ResponseSpec {
        return webClient()
            .post()
            .uri {
                it
                    .queryParam("database", db.value)
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
    }

}
