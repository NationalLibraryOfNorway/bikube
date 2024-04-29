package no.nb.bikube.catalogue.collections.repository

import no.nb.bikube.catalogue.collections.config.CollectionsWebClient
import no.nb.bikube.catalogue.collections.enum.*
import no.nb.bikube.catalogue.collections.exception.CollectionsException
import no.nb.bikube.catalogue.collections.model.CollectionsLocationModel
import no.nb.bikube.catalogue.collections.model.CollectionsModel
import no.nb.bikube.catalogue.collections.model.CollectionsNameModel
import no.nb.bikube.catalogue.collections.model.CollectionsTermModel
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.util.logger
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Repository
class CollectionsRepository(
    private val collectionsWebClient: CollectionsWebClient
) {
    fun webClient() = collectionsWebClient.webClient()

    @Throws(CollectionsException::class)
    fun getSingleCollectionsModel(titleCatalogId: String): Mono<CollectionsModel> {
        return searchTexts("priref=${titleCatalogId}")
    }

    @Throws(CollectionsException::class)
    fun getSingleCollectionsModelWithoutChildren(titleCatalogId: String): Mono<CollectionsModel> {
        val fields = "priref and title and work.description_type and record_type " +
                "and dating.date.start and dating.date.end and publisher " +
                "and association.geographical_keyword and language and submedium " +
                "and format and alternative_number and alternative_number.type " +
                "and part_of_reference and PID_data_URN and current_location.barcode"

        return getRecordsWebClientRequest("priref=${titleCatalogId}", CollectionsDatabase.TEXTS, fields).bodyToMono<CollectionsModel>()
    }

    fun getAllNewspaperTitles(page: Int = 1): Mono<CollectionsModel> {
        return getRecordsWebClientRequest(
            "record_type=${CollectionsRecordType.WORK}",
            CollectionsDatabase.TEXTS,
            limit = 50,
            from = (page-1) * 50 + 1
        ).bodyToMono<CollectionsModel>()
    }

    fun getManifestationsByDateAndTitle(date: LocalDate, titleCatalogId: String): Mono<CollectionsModel> {
        val dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
        return searchTexts(
            "record_type=${CollectionsRecordType.MANIFESTATION} and " +
            "part_of_reference.lref=${titleCatalogId} and " +
            "dating.date.start='${dateString}'"
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

    fun searchLocationAndContainers(barcode: String): Mono<CollectionsLocationModel> {
        return searchLocationDatabase("barcode=${barcode}")
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

    fun createLocationRecord(serializedBody: String): Mono<CollectionsLocationModel> {
        return createRecordWebClientRequest(serializedBody, CollectionsDatabase.LOCATIONS).bodyToMono<CollectionsLocationModel>()
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

    private fun searchLocationDatabase(query: String): Mono<CollectionsLocationModel> {
        return getRecordsWebClientRequest(query, CollectionsDatabase.LOCATIONS).bodyToMono<CollectionsLocationModel>()
    }

    private fun getRecordsWebClientRequest(
        query: String,
        db: CollectionsDatabase,
        fields: String? = null,
        limit: Int = 10,
        from: Int = 1
    ): WebClient.ResponseSpec {
        return webClient()
            .get()
            .uri {
                val params = it
                    .queryParam("database", db.value)
                    .queryParam("output", "json")
                    .queryParam("search", query)
                    .queryParam("limit", limit)
                    .queryParam("startfrom", from)

                if (fields != null) {
                    params.queryParam("fields", fields)
                }

                params.build()
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
