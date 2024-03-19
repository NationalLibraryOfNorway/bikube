package no.nb.bikube.catalogue.alma.service

import no.nb.bikube.catalogue.alma.config.AlmaConfig
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.model.MarcRecord
import no.nb.bikube.catalogue.alma.model.RecordList
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class AlmaSruService(
    private val almaConfig: AlmaConfig,
    private val marcXChangeService: MarcXChangeService
) {

    private val webClient = WebClient.builder()
        .baseUrl(almaConfig.almaSruUrl)
        .build()

    private fun getRecordsWithFieldValue(field: String, value: String): Mono<String> {
        return webClient.get()
            .uri { builder ->
                builder.queryParam("query", "$field=$value")
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
                                !recordMatches(record, "035", "a") { s -> s.startsWith("(CKB)") }
                    }
            }
            .handle { marcRecords, sink ->
                if (marcRecords.isEmpty())
                    sink.error(AlmaRecordNotFoundException("No record found for ISSN $issn"))
                else
                    sink.next(RecordList(marcRecords))
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

    private fun identicalIssn(subfieldContent: String?, issn: String): Boolean {
        return subfieldContent?.replace("-", "").equals(issn.replace("-", ""))
    }
}
