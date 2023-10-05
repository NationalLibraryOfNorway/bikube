package no.nb.bikube.core.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.enum.CatalogueName
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.enum.materialTypeToCatalogueName
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.exception.AxiellTitleNotFound
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Tag(name = "Catalogue objects", description = "Endpoints related to catalog data for all text material")
@RequestMapping("")
class CoreController (
    private val axiellService: AxiellService
){
    @GetMapping("/item", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single item from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class, NotSupportedException::class)
    fun getSingleItem(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Item>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.getSingleItem(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/title", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single title from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(AxiellCollectionsException::class, AxiellTitleNotFound::class, NotSupportedException::class)
    fun getSingleTitle(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Title>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.getSingleTitle(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }
}
