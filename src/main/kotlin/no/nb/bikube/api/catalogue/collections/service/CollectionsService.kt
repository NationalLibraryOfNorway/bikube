package no.nb.bikube.api.catalogue.collections.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.api.catalogue.collections.config.CollectionsWebClientConfig
import no.nb.bikube.api.catalogue.collections.enum.*
import no.nb.bikube.api.catalogue.collections.exception.CollectionsException
import no.nb.bikube.api.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.model.dto.CollectionsLocationDto
import no.nb.bikube.api.catalogue.collections.model.dto.createContainerDto
import no.nb.bikube.api.core.util.logger
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// No bean annotation here, as this is configured in CollectionsServiceConfig.kt (no/nb/bikube/catalogue/collections/config/CollectionsServiceConfig.kt)
class CollectionsService(
    val collectionsWebClient: CollectionsWebClientConfig,
    val collectionsDatabase: CollectionsDatabase
) {
    fun collectionsWebClient() = collectionsWebClient.collectionsWebClient()

    @Throws(CollectionsException::class)
    fun getSingleCollectionsModel(titleCatalogId: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return getRecordsWebClientRequest("priref=${titleCatalogId}", db).bodyToMono<CollectionsModel>()
    }

    @Throws(CollectionsException::class)
    fun getSingleCollectionsModelWithoutChildren(titleCatalogId: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        val fields = "priref and title and work.description_type and record_type " +
                "and dating.date.start and dating.date.end and edition.date and publisher " +
                "and association.geographical_keyword and language and submedium " +
                "and format and alternative_number and alternative_number.type " +
                "and part_of_reference and PID_data_URN and current_location.barcode " +
                "and related_object.title and related_object.association and related_object.reference " +
                "and related_object.reference.lref and related_object.record_type"

        return getRecordsWebClientRequest("priref=${titleCatalogId}", db, fields).bodyToMono<CollectionsModel>()
    }

    fun getAllWorks(page: Int = 1, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return getRecordsWebClientRequest(
            "record_type=${CollectionsRecordType.WORK}",
            db,
            limit = 50,
            from = (page-1) * 50 + 1
        ).bodyToMono<CollectionsModel>()
    }

    fun getManifestations(
        date: LocalDate,
        titleCatalogId: String,
        volume: String? = null,
        number: String? = null,
        version: String? = null,
        db: CollectionsDatabase = collectionsDatabase
    ): Mono<CollectionsModel> {
        val dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
        val edition = listOfNotNull(
            volume?.takeIf { it.isNotBlank() } ?: "U",
            number?.takeIf { it.isNotBlank() } ?: "U",
            version?.takeIf { it.isNotBlank() } ?: "U"
        ).joinToString("-")
        val editionQuery = if (edition == "U-U-U") {
            " and not edition='*'" // equivalent to " and edition = null", but that isn't supported in Collections
        } else {
            " and edition='$edition'"
        }

        return getRecordsWebClientRequest(
            "record_type=${CollectionsRecordType.MANIFESTATION} and " +
            "part_of_reference.lref=${titleCatalogId} and " +
            "edition.date='${dateString}'" +
            editionQuery,
            db
        ).bodyToMono<CollectionsModel>()
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
    fun createRecord(serializedBody: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsModel>()
    }

    fun updateRecord(serializedBody: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return updateRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsModel>()
    }

    fun deleteRecord(id: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return deleteRecordWebClientRequest(id, db).bodyToMono<CollectionsModel>()
    }

    fun search(query: String, db: CollectionsDatabase = collectionsDatabase): Mono<CollectionsModel> {
        return getRecordsWebClientRequest(query, db).bodyToMono<CollectionsModel>()
    }

    fun createNameRecord(serializedBody: String, db: CollectionsDatabase): Mono<CollectionsNameModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsNameModel>()
    }

    fun createTermRecord(serializedBody: String, db: CollectionsDatabase): Mono<CollectionsTermModel> {
        return createRecordWebClientRequest(serializedBody, db).bodyToMono<CollectionsTermModel>()
    }

    fun createContainerIfNotExists(
        barcode: String,
        username: String
    ): Mono<CollectionsLocationObject> {
        return getRecordsWebClientRequest("barcode=${barcode}", CollectionsDatabase.LOCATIONS)
            .bodyToMono<CollectionsLocationModel>()
            .flatMap {
                if (it.hasObjects()) {
                    Mono.just(it.getFirstObject())
                } else {
                    createLocationRecord(barcode, username)
                }
            }
    }

    fun createLocationRecord(
        barcode: String,
        username: String,
    ): Mono<CollectionsLocationObject> {
        val dto: CollectionsLocationDto = createContainerDto(barcode, username, null)
        val encodedBody = Json.encodeToString(dto)
        return createRecordWebClientRequest(encodedBody, CollectionsDatabase.LOCATIONS)
            .bodyToMono<CollectionsLocationModel>()
            .handle { collectionsLocationModel, sink ->
                if (collectionsLocationModel.hasObjects())
                    sink.next(collectionsLocationModel.getFirstObject())
                else
                    sink.error(CollectionsItemNotFound("New container not found"))
            }
    }

    protected fun searchNameDatabases(query: String): Mono<CollectionsNameModel> {
        return getRecordsWebClientRequest(query, CollectionsDatabase.PEOPLE).bodyToMono<CollectionsNameModel>()
    }

    protected fun searchTermDatabases(query: String, db: CollectionsDatabase): Mono<CollectionsTermModel> {
        return getRecordsWebClientRequest(query, db).bodyToMono<CollectionsTermModel>()
    }

    protected fun getRecordsWebClientRequest(
        query: String,
        db: CollectionsDatabase,
        fields: String? = null,
        limit: Int = 10,
        from: Int = 1
    ): WebClient.ResponseSpec {
        return collectionsWebClient()
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

    protected fun createRecordWebClientRequest(serializedBody: String, db: CollectionsDatabase): WebClient.ResponseSpec {
        return collectionsWebClient()
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

    protected fun updateRecordWebClientRequest(serializedBody: String, db: CollectionsDatabase): WebClient.ResponseSpec {
        return collectionsWebClient()
            .post()
            .uri {
                it
                    .queryParam("database", db.value)
                    .queryParam("command", "updaterecord")
                    .queryParam("output", "json")
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(serializedBody)
            .retrieve()
            .onStatus(
                { it.is4xxClientError || it.is5xxServerError },
                { Mono.error(CollectionsException("Error updating catalog item")) }
            )
    }

    protected fun deleteRecordWebClientRequest(id: String, db: CollectionsDatabase): WebClient.ResponseSpec {
        return collectionsWebClient()
            .post()
            .uri {
                it
                    .queryParam("database", db.value)
                    .queryParam("command", "deleterecord")
                    .queryParam("output", "json")
                    .queryParam("priref", id)
                    .build()
            }
            .retrieve()
            .onStatus(
                { it.is4xxClientError || it.is5xxServerError },
                { Mono.error(CollectionsException("Error deleting catalog item")) }
            )
    }
}
