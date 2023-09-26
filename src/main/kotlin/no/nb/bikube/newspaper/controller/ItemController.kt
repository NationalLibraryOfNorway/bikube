package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.Item
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@Tag(name="Newspaper items", description="Endpoints related to newspaper items.")
@RequestMapping("/newspapers/items")
class ItemController (
    private val axiellService: AxiellService
){
    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get all newspaper items")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(AxiellCollectionsException::class)
    fun getAllItems(
        @RequestParam(required = false) titleCatalogueId: String?,
    ): ResponseEntity<Flux<Item>> {
        return titleCatalogueId?.let {
            ResponseEntity.ok(axiellService.getItemsForTitle(it))
        } ?: ResponseEntity.ok(axiellService.getAllItems())
    }
}