package no.nb.bikube.catalogue.alma.repository

import no.nb.bikube.catalogue.alma.config.AlmaConfig
import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.model.AlmaErrorCode
import no.nb.bikube.catalogue.alma.service.MarcXChangeService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

@Repository
class AlmaRepository(
    private val almaConfig: AlmaConfig,
    private val marcXChangeService: MarcXChangeService
) {
    private val webClient = WebClient.builder()
        .baseUrl(almaConfig.almawsUrl)
        .build()

    fun getRecordByMMS(mms: String, prolog: Boolean): Mono<ByteArray> {
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
            .map { marcXChangeService.parseBibResult(it, prolog) }
    }

    private fun mapHttpError(response: ClientResponse): Mono<Throwable> {
        return when (response.statusCode()) {
            HttpStatus.BAD_REQUEST -> response.bodyToMono(String::class.java)
                .map(marcXChangeService::parseErrorResponse)
                .map { errorResponse ->
                    errorResponse.errorList
                        .find { it.errorCode == AlmaErrorCode.NOT_FOUND.value }
                        ?. let { AlmaRecordNotFoundException(it.errorMessage) }
                        ?: errorResponse.errorList
                        .find { it.errorCode == AlmaErrorCode.ILLEGAL_ARG.value }
                        ?. let { IllegalArgumentException(it.errorMessage) }
                        ?: AlmaException(errorResponse.errorList.map { it.errorMessage }.toString())
                }
            else -> response.bodyToMono(String::class.java)
                .map { AlmaException(it) }
        }
    }
}
