package no.nb.bikube.catalogue.collections.repository

import no.nb.bikube.core.enum.*
import no.nb.bikube.catalogue.collections.exception.CollectionsException
import no.nb.bikube.catalogue.collections.model.CollectionsModel
import no.nb.bikube.catalogue.collections.model.CollectionsNameModel
import no.nb.bikube.catalogue.collections.model.CollectionsTermModel
import no.nb.bikube.core.util.logger
import no.nb.bikube.catalogue.collections.config.CollectionsWebClient
import no.nb.bikube.catalogue.collections.enum.*
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Repository
class CollectionsRepository(
    private val collectionsWebClient: CollectionsWebClient
) {
    fun webClient() = collectionsWebClient.webClient()

    @Throws(CollectionsException::class)
    fun getSingleCollectionsModel(titleCatalogId: String): Mono<CollectionsModel> {
        return searchTexts("priref=${titleCatalogId}")
    }

    fun getWorkYearForTitle(titleCatalogId: String, year: Int): Mono<CollectionsModel> {
        return searchTexts(
            "part_of_reference.lref=${titleCatalogId} and " +
            "dating.date.start=${year} and " +
            "work.description_type=${CollectionsDescriptionType.YEAR}"
        )
    }

    fun getTitleByName(name: String): Mono<CollectionsModel> {
        return searchTexts(
            "record_type=${CollectionsRecordType.WORK} and " +
            "work.description_type=${CollectionsDescriptionType.SERIAL} and " +
            "title=\"${name}\""
        )
    }

    fun searchPublisher(name: String): Mono<CollectionsNameModel> {
        return searchNameDatabases("name.type=${CollectionsNameType.PUBLISHER} and name=\"${name}\"")
    }

    fun searchLanguage(name: String): Mono<CollectionsTermModel> {
        return searchTermDatabases(
            "term.type=${CollectionsTermType.LANGUAGE} and term=\"${name}\"",
            CollectionsDatabase.LANGUAGES
        )
    }

    fun searchPublisherPlace(name: String): Mono<CollectionsTermModel> {
        return searchTermDatabases("term=\"${name}\" and term.type=\"place\"", CollectionsDatabase.GEO_LOCATIONS)
    }

    @Throws(CollectionsException::class)
    fun createTextsRecord(serializedBody: String): Mono<CollectionsModel> {
        return createRecordWebClientRequest(serializedBody, CollectionsDatabase.TEXTS).bodyToMono<CollectionsModel>()
    }

    fun createNameRecord(serializedBody: String, db: CollectionsDatabase): Mono<CollectionsNameModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsNameModel>()
    }

    fun createTermRecord(serializedBody: String, db: CollectionsDatabase): Mono<CollectionsTermModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsTermModel>()
    }

    private fun searchTexts(query: String): Mono<CollectionsModel> {
        return getRecordsWebClientRequest(query, CollectionsDatabase.TEXTS).bodyToMono<CollectionsModel>()
    }

    private fun searchNameDatabases(query: String): Mono<CollectionsNameModel> {
        return getRecordsWebClientRequest(query, CollectionsDatabase.PEOPLE).bodyToMono<CollectionsNameModel>()
    }

    private fun searchTermDatabases(query: String, db: CollectionsDatabase): Mono<CollectionsTermModel> {
        return getRecordsWebClientRequest(query, db).bodyToMono<CollectionsTermModel>()
    }

    private fun getRecordsWebClientRequest(query: String, db: CollectionsDatabase): WebClient.ResponseSpec {
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
                    Mono.error(CollectionsException(
                        "Could not search in Collections catalogue. Try again later or contact Team Text if the problem persists."
                    ))
                }
            )
    }

    private fun createRecordWebClientRequest(serializedBody: String, db: CollectionsDatabase): WebClient.ResponseSpec {
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
                { Mono.error(CollectionsException("Error creating title")) }
            )
    }

}
