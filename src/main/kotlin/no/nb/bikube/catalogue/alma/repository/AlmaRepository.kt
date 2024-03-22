package no.nb.bikube.catalogue.alma.repository

import no.nb.bikube.catalogue.alma.config.AlmaConfig
import no.nb.bikube.catalogue.alma.config.AlmaHttpConnector
import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.model.AlmaBibResult
import no.nb.bikube.catalogue.alma.model.AlmaErrorCode
import no.nb.bikube.catalogue.alma.model.MarcRecord
import no.nb.bikube.catalogue.alma.service.MarcXChangeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Repository
class AlmaRepository(
    private val almaHttpConnector: AlmaHttpConnector,
    private val almaConfig: AlmaConfig,
    private val marcXChangeService: MarcXChangeService
) {

    private val webClient = WebClient.builder()
        .clientConnector(almaHttpConnector.httpConnector())
        .baseUrl(almaConfig.almawsUrl)
        .build()

    fun getRecordByMMS(mms: String): Mono<AlmaBibResult> {
        return webClient.get()
            .uri { builder ->
                builder.path("/bibs/$mms")
                    .queryParam("expand", "None")
                    .queryParam("apiKey", almaConfig.apiKey)
                    .build()
            }
            .accept(MediaType.APPLICATION_XML, MediaType.TEXT_XML)
            .retrieve()
            .onStatus(
                { it != HttpStatus.OK },
                { response -> mapHttpError(response) }
            )
            .bodyToMono(String::class.java)
            .map { marcXChangeService.parseBibResult(it) }
            .onErrorMap { throwable ->
                if (throwable is AlmaException || throwable is IllegalArgumentException)
                    throwable
                else
                    AlmaException("Encountered an error [${throwable.message}]").initCause(throwable)
            }
    }

    fun getRecordByBarcode(barcode: String): Mono<MarcRecord> {
        return webClient.get()
            .uri { builder ->
                builder.path("/items")
                    .queryParam("item_barcode", barcode)
                    .queryParam("expand", "None")
                    .queryParam("apiKey", almaConfig.apiKey)
                    .build()
            }
            .accept(MediaType.APPLICATION_XML, MediaType.TEXT_XML)
            .retrieve()
            .onStatus(
                { it != HttpStatus.OK },
                { response -> mapHttpError(response) }
            )
            .bodyToMono(String::class.java)
            .handle { s, sink ->
                val item = marcXChangeService.parseItemResult(s)
                if (item.bibData.mmsId.isNotEmpty())
                    sink.next(item)
                else
                    sink.error(AlmaException("No MMS-id found in Alma item result"))
            }
            .flatMap { item ->
                getRecordByMMS(item.bibData.mmsId)
                    .map { bib ->
                        marcXChangeService.addEnumChron(item.itemData, bib.record)
                    }
            }
            .onErrorMap { throwable ->
                if (throwable is AlmaException || throwable is IllegalArgumentException)
                    throwable
                else
                    AlmaException("Encountered an error [${throwable.message}]").initCause(throwable)
            }
    }

    private fun mapHttpError(response: ClientResponse): Mono<Throwable> {
        if (response.statusCode() != HttpStatus.BAD_REQUEST)
            return response.bodyToMono(String::class.java)
                .map { AlmaException(it) }

        return response.bodyToMono(String::class.java)
            .map(marcXChangeService::parseErrorResponse)
            .handle { errorResponse, sink ->
                val notFoundError = errorResponse.errorList.firstOrNull {
                    it.errorCode == AlmaErrorCode.MMS_NOT_FOUND.value || it.errorCode == AlmaErrorCode.BARCODE_NOT_FOUND.value
                }
                val illegalArgError = errorResponse.errorList.firstOrNull {
                    it.errorCode == AlmaErrorCode.ILLEGAL_ARG.value
                }
                if (notFoundError != null)
                    sink.next(AlmaRecordNotFoundException(notFoundError.errorMessage))
                else if (illegalArgError != null)
                    sink.next(IllegalArgumentException(illegalArgError.errorMessage))
                else
                    sink.next(AlmaException(errorResponse.errorList.map { it.errorMessage }.toString()))
            }
    }
}
