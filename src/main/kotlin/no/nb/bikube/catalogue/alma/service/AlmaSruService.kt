package no.nb.bikube.catalogue.alma.service

import no.nb.bikube.catalogue.alma.config.AlmaConfig
import no.nb.bikube.catalogue.alma.config.AlmaHttpConnector
import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.model.MarcRecord
import no.nb.bikube.catalogue.alma.model.RecordList
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class AlmaSruService(
    private val almaHttpConnector: AlmaHttpConnector,
    private val almaConfig: AlmaConfig,
    private val marcXChangeService: MarcXChangeService
) {

    private val webClient = WebClient.builder()
        .clientConnector(almaHttpConnector.httpConnector())
        .baseUrl(almaConfig.almaSruUrl)
        .build()

    private fun getRecordsWithFieldValue(field: String, value: String): Mono<String> {
        return webClient.get()
            .uri { builder ->
                builder.path("/47BIBSYS_NB")
                    .queryParams(almaConfig.commonParams)
                    .queryParam("query", "$field=$value")
                    .build()
            }
            .accept(MediaType.TEXT_XML)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    fun getRecordsByISSN(issn: String): Mono<RecordList> {
        return getRecordsWithFieldValue("issn", issn)
            .map {
                val result = marcXChangeService.parseSruResult(it)
                result.records
                    .map { record -> record.recordData.record }
                    .filter { record ->
                        recordMatches(record, "022", "a") { s -> identicalIssn(s, issn) } &&
                                !isFromCentralKnowledgeBase(record)
                    }
            }
            .handle { marcRecords, sink ->
                if (marcRecords.isEmpty())
                    sink.error(AlmaRecordNotFoundException("No record found for ISSN $issn"))
                else
                    sink.next(RecordList(marcRecords))
            }
    }

    fun getRecordsByISBN(isbn: String): Mono<RecordList> {
        return getRecordsWithFieldValue("isbn", isbn)
            .map {
                val result = marcXChangeService.parseSruResult(it)
                result.records
                    .map { record -> record.recordData.record }
                    .filter { record -> !isFromCentralKnowledgeBase(record) }
            }
            .handle { marcRecords, sink ->
                if (marcRecords.isEmpty())
                    sink.error(AlmaRecordNotFoundException("No record found for ISBN $isbn"))
                else
                    sink.next(RecordList(marcRecords))
            }
    }

    fun getRecordsByISMN(ismn: String): Mono<RecordList> {
        return getRecordsWithFieldValue("stored_standard_number_024_a_ind1_2", ismn)
            .map {
                val result = marcXChangeService.parseSruResult(it)
                result.records
                    .map { record -> record.recordData.record }
                    .filter { record -> !isFromCentralKnowledgeBase(record) }
            }
            .handle { marcRecords, sink ->
                if (marcRecords.isEmpty())
                    sink.error(AlmaRecordNotFoundException("No record found for ISMN $ismn"))
                else
                    sink.next(RecordList(marcRecords))
            }
    }

    fun searchRecords(title: String, author: String?, publishYear: Int): Mono<RecordList> {
        val queryString = "title all \"$title\" " +
                (author ?. let {"creator all \"$author\" "} ?: "") +
                "main_pub_date==$publishYear"
        return webClient
            .get()
            .uri { builder ->
                builder.path("/47BIBSYS_NETWORK")
                    .queryParams(almaConfig.commonParams)
                    .queryParam("query", queryString)
                    .build()
            }
            .accept(MediaType.TEXT_XML)
            .retrieve()
            .bodyToMono(String::class.java)
            .map {
                val marcRecords = marcXChangeService.parseSruResult(it)
                    .records
                    .map { records ->
                        records.recordData.record
                    }
                RecordList(marcRecords)
            }
            .onErrorResume {
                Mono.error(AlmaException("Error while searching: ${it.message}"))
            }
    }

    private fun recordMatches(
        record: MarcRecord,
        fieldTag: String,
        subfieldCode: String,
        predicate: (String) -> Boolean
    ): Boolean {
        return record.datafield
            .filter { field -> field.tag == fieldTag }
            .flatMap { field -> field.subfield }
            .filter { subfield -> subfield.code == subfieldCode }
            .any { subfield ->
                subfield.content ?. let { predicate(it) } ?: false
            }
    }

    private fun isFromCentralKnowledgeBase(record: MarcRecord): Boolean {
        return recordMatches(record, "035", "a") { s -> s.startsWith("(CKB)") }
    }

    private fun identicalIssn(subfieldContent: String?, issn: String): Boolean {
        return subfieldContent?.replace("-", "").equals(issn.replace("-", ""))
    }
}
