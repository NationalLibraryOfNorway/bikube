package no.nb.bikube.core.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import no.nb.bikube.core.enum.CatalogueName
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.enum.materialTypeToCatalogueName
import no.nb.bikube.core.exception.CollectionsException
import no.nb.bikube.core.exception.CollectionsTitleNotFound
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.model.CatalogueRecord
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Title
import no.nb.bikube.newspaper.service.CollectionsService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@Tag(name = "Catalogue objects", description = "Endpoints related to catalog data for all text material")
@RequestMapping("")
class CoreController (
    private val collectionsService: CollectionsService
){
    companion object {
        const val DATE_REGEX = "^(17|18|19|20)\\d{2}(-)?(0[1-9]|1[0-2])(-)?(0[1-9]|[12][0-9]|3[01])$"
    }

    @GetMapping("/item", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single item from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(CollectionsException::class, CollectionsTitleNotFound::class, NotSupportedException::class)
    fun getSingleItem(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Item>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(collectionsService.getSingleItem(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/title", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single title from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    @Throws(CollectionsException::class, CollectionsTitleNotFound::class, NotSupportedException::class)
    fun getSingleTitle(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Title>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(collectionsService.getSingleTitle(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/title/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search catalogue titles",
        description = "Searches catalogue titles by name. Supports wildcard '*' search."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun searchTitle(
        @RequestParam searchTerm: String,
        @RequestParam materialType: MaterialType
    ): ResponseEntity<Flux<CatalogueRecord>> {
        if (searchTerm.isEmpty()) throw BadRequestBodyException("Search term cannot be empty.")
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(collectionsService.searchTitleByName(searchTerm))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/item/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search catalogue items",
        description = "Searches catalogue items by name. Supports wildcard '*' search."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun searchItem(
        @RequestParam
        @Schema(description = "The term to search for. Supports wildcard '*' search.")
        searchTerm: String,
        @RequestParam materialType: MaterialType,
        @RequestParam
        @Pattern(regexp = DATE_REGEX)
        @Schema(description = "Date must be in ISO-8601 format (YYYY-MM-DD).")
        date: String? = null,
        @RequestParam
        isDigital: Boolean? = null,
    ): ResponseEntity<Flux<CatalogueRecord>> {
        if (searchTerm.isEmpty()) throw BadRequestBodyException("Search term cannot be empty.")
        validateDate(date ?: "")

        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(
                axiellService.searchItemByName(searchTerm, LocalDate.parse(date), isDigital)
            )
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    private fun validateDate(date: String) {
        runCatching { LocalDate.parse(date) }
            .getOrElse { throw BadRequestBodyException("Date must valid ISO-8601 format") }
    }
}
