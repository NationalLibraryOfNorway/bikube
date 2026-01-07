package no.nb.bikube.api.catalogue.collections.controller

import no.nb.bikube.api.catalogue.collections.config.CollectionsWebClientConfig
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class CollectionsRawController(
    private val collectionsWebClientConfig: CollectionsWebClientConfig
) {
    private val baseUrl = "https://api.collections.stage.nb.no/webapi_tekst/wwwopac.ashx"

    @GetMapping("/api/collections/raw")
    fun getRawCollections(
        @RequestParam params: String
    ): Mono<ResponseEntity<String>> {
        val url = "$baseUrl?$params"
        val webClient = collectionsWebClientConfig.collectionsWebClient()
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { ResponseEntity.ok(it) }
    }
}

