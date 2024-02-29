package no.nb.bikube.catalogue.alma.repository

import no.nb.bikube.catalogue.alma.config.AlmaConfig
import no.nb.bikube.catalogue.alma.service.MarcXChangeService
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
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
            .bodyToMono(String::class.java)
            .map { marcXChangeService.parseBibResult(it, prolog) }
        // TODO: Error handling
    }
}
