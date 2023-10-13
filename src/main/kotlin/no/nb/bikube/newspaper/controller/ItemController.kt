package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.service.CreationValidationService
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@Tag(name="Newspaper items", description="Endpoints related to newspaper items.")
@RequestMapping("/newspapers/items")
class ItemController (
    private val axiellService: AxiellService,
    private val creationValidationService: CreationValidationService
){
    @GetMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get all newspaper items")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "404", description = "Title not found"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(AxiellCollectionsException::class)
    @Deprecated("Will be removed in a future version as getting all items will have too much data.")
    fun getAllItems(
        @RequestParam(required = false) titleCatalogueId: String?,
    ): Mono<ResponseEntity<List<Item>>> {
        val results = titleCatalogueId?.let {
            axiellService.getItemsForTitle(it)
        } ?: axiellService.getAllItems()

        return results
            .collectList()
            .map { ResponseEntity.ok(it) }
    }

    @PostMapping("/", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a single newspaper item")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Newspaper created"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createItem(
        @RequestBody item: Item
    ): Mono<ResponseEntity<Item>> {
        creationValidationService.validateItem(item)

        // Checks that title exists before creating item. Will throw exception if not found.
        return axiellService.getSingleTitle(item.titleCatalogueId!!)
            .flatMap { axiellService.createNewspaperItem(item) }
            .map { ResponseEntity.ok(it) }
    }
}
