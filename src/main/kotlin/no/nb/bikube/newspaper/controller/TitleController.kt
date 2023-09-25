package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

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
    fun getTitles(): Flux<Title> {
        return axiellService.getTitles()
    }
}