package no.nb.bikube.newspaper.service

import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.core.model.CollectionsModel
import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.config.AxiellConfig
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux

@Service
class AxiellService  (
    axiellConfig: AxiellConfig
) {

    private val webClient = WebClient.builder().baseUrl(axiellConfig.url).build()

    fun getTitles(): Flux<Title> {
        return webClient
            .get()
            .uri {
                it
                    .queryParam("database", "texts")
                    .queryParam(
                        "search", "" +
                            "record_type=${AxiellRecordType.WORK} and " +
                            "work.description_type=${AxiellDescriptionType.SERIAL}"
                    )
                    .queryParam("output", "json")
                    .build()
            }
            .retrieve()
            .bodyToMono<CollectionsModel>()
            .flatMapIterable { it.adlibJson.recordList }
            .map { mapCollectionsObjectToGenericTitle(it) }

    }
}