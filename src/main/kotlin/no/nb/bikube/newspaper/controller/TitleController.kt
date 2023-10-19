package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.RecordAlreadyExistsException
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.inputDto.TitleInputDto
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Tag(name="Newspaper titles", description="Endpoints related to newspaper titles.")
@RequestMapping("/newspapers/titles")
class TitleController (
    private val axiellService: AxiellService
) {
    @PostMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a newspaper title")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Title created"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createTitle(
        @RequestBody title: TitleInputDto
    ): Mono<ResponseEntity<Title>> {
        logger().info("Trying to create newspaper title: $title")
        return if (title.name.isNullOrEmpty()) {
            Mono.error(BadRequestBodyException("Title name cannot be null or empty"))
        } else if (title.startDate != null && title.endDate != null && title.startDate.isAfter(title.endDate)) {
            Mono.error(BadRequestBodyException("Start date cannot be after end date"))
        } else {
            val publisherMono: Mono<Publisher> = title.publisher?.let { publisherName ->
                axiellService.createPublisher(publisherName).onErrorResume { exception ->
                    if (exception is RecordAlreadyExistsException) Mono.empty()
                    else Mono.error(exception)
                }
            } ?: Mono.empty()

            val locationMono: Mono<PublisherPlace> = title.publisherPlace?.let { locationName ->
                axiellService.createPublisherPlace(locationName).onErrorResume { exception ->
                    if (exception is RecordAlreadyExistsException) Mono.empty()
                    else Mono.error(exception)
                }
            } ?: Mono.empty()

            val languageMono: Mono<Language> = title.language?.let { languageName ->
                axiellService.createLanguage(languageName).onErrorResume { exception ->
                    if (exception is RecordAlreadyExistsException) Mono.empty()
                    else Mono.error(exception)
                }
            } ?: Mono.empty()

            return Mono.`when`(publisherMono, locationMono, languageMono)
                .then(axiellService.createNewspaperTitle(title))
                .map { createdTitle -> ResponseEntity.status(HttpStatus.CREATED).body(createdTitle) }
                .doOnSuccess { responseEntity ->
                    logger().info("Newspaper title created: ${responseEntity.body?.catalogueId}")
                }
        }
    }
}
