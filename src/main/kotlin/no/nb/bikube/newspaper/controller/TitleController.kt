package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.collect
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.Publisher
import no.nb.bikube.core.model.PublisherPlace
import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@Tag(name="Newspaper titles", description="Endpoints related to newspaper titles.")
@RequestMapping("/newspapers/titles")
class TitleController (
    private val axiellService: AxiellService
) {
    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get all newspaper titles")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(AxiellCollectionsException::class)
    @Deprecated("Will be removed in a future version as getting all titles will have too much data.")
    fun getTitles(): ResponseEntity<Flux<Title>> {
        return ResponseEntity.ok(axiellService.getTitles())
    }

    @PostMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a newspaper title")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createTitle(
        @RequestBody title: Title
    ): Mono<ResponseEntity<Title>> {
        return if (title.name.isNullOrEmpty()) {
            Mono.just(ResponseEntity.badRequest().build())
        } else {
            val publisherMono: Mono<Publisher> = title.publisher?.let {
                axiellService.createPublisher(it).onErrorResume { Mono.empty() }
            } ?: Mono.empty()

            val locationMono: Mono<PublisherPlace> = title.publisherPlace?.let {
                axiellService.createPublisherPlace(it).onErrorResume { Mono.empty() }
            } ?: Mono.empty()

            val languageMono: Mono<PublisherPlace> = title.language?.let {
                axiellService.createLanguage(it).onErrorResume { Mono.empty() }
            } ?: Mono.empty()

            return Mono.`when`(publisherMono, locationMono, languageMono)
                .then(axiellService.createTitle(title))
                .map { createdTitle -> ResponseEntity.ok(createdTitle) }
        }
    }
}
