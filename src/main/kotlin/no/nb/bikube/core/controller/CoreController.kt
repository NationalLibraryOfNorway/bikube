package no.nb.bikube.core.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.catalogue.collections.exception.CollectionsException
import no.nb.bikube.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.core.enum.CatalogueName
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.enum.materialTypeToCatalogueName
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.model.CatalogueRecord
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.service.SearchFilterService
import no.nb.bikube.newspaper.service.NewspaperService
import no.nb.bikube.newspaper.service.TitleIndexService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URL
import java.time.LocalDate

@RestController
@Tag(name = "Catalogue objects", description = "Endpoints related to catalog data for all text material")
@RequestMapping("")
class CoreController (
    private val newspaperService: NewspaperService,
    private val titleIndexService: TitleIndexService,
    private val searchFilterService: SearchFilterService
){
    companion object {
        const val DATE_REGEX = "^(17|18|19|20)\\d{2}(-)?(0[1-9]|1[0-2])(-)?(0[1-9]|[12][0-9]|3[01])$"
    }

    @GetMapping("/item", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single item from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    @Throws(CollectionsException::class, CollectionsTitleNotFound::class, NotSupportedException::class)
    fun getSingleItem(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Item>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(newspaperService.getSingleItem(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/title", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get single title from catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    @Throws(CollectionsException::class, CollectionsTitleNotFound::class, NotSupportedException::class)
    fun getSingleTitle(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType,
    ): ResponseEntity<Mono<Title>> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(newspaperService.getSingleTitle(catalogueId))
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
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun searchTitle(
        @Parameter(description = "Search term")
        @RequestParam searchTerm: String,
        @Parameter(description = "Material type of the titles to search for")
        @RequestParam materialType: MaterialType,
        @Parameter(description = "Date in ISO-8601 format (YYYY-MM-DD). " +
                "If provided, only titles with start and end date that includes this date will be returned.")
        @RequestParam date: LocalDate? = null,
        @Parameter(description = "Whether or not to select only the best match for search term")
        @RequestParam selectBestMatch: Boolean = false
    ): ResponseEntity<List<CatalogueRecord>> {
        if (searchTerm.isEmpty()) throw BadRequestBodyException("Search term cannot be empty.")
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> {
                val searchResult = titleIndexService.searchTitle(searchTerm)
                ResponseEntity.ok(searchFilterService.filterSearchResults(searchResult, searchTerm, date, selectBestMatch))
            }
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/item/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search catalogue items",
        description = "Search catalogue items by title id, material type, date and digital/physical."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    @Parameters(
        Parameter(name = "titleCatalogueId", description = "Catalogue ID of the title to search items for."),
        Parameter(name = "materialType", description = "Material type of the items to search for."),
        Parameter(name = "date", description = "Date in ISO 8601 format (YYYY-MM-DD).", schema = Schema(pattern = DATE_REGEX)),
        Parameter(name = "isDigital", description = "If 'true' returns only digital items. If 'false', returns only physical items. Default is all.")
    )
    fun searchItem(
        @RequestParam(required = true) titleCatalogueId: String,
        @RequestParam(required = true) materialType: MaterialType,
        @RequestParam(required = true) date: String,
        @RequestParam(required = false) isDigital: Boolean? = null,
    ): ResponseEntity<Flux<CatalogueRecord>> {
        if (titleCatalogueId.isEmpty()) throw BadRequestBodyException("Search term cannot be empty.")

        val parsedDate: LocalDate = date.takeIf { it.isNotEmpty() }?.let {
            runCatching { LocalDate.parse(date) }
                .getOrElse { throw BadRequestBodyException("Date must valid ISO-8601 format") }
        } ?: throw BadRequestBodyException("Date cannot be empty.")

        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(
                newspaperService.getItemsByTitleAndDate(titleCatalogueId, parsedDate, isDigital)
            )
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }

    @GetMapping("/title/link")
    @Operation(summary = "Get a direct link to title in catalogue")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()])
    ])
    @Throws(NotSupportedException::class)
    fun getCatalogueLink(
        @RequestParam catalogueId: String,
        @RequestParam materialType: MaterialType
    ): ResponseEntity<URL> {
        return when(materialTypeToCatalogueName(materialType)) {
            CatalogueName.COLLECTIONS -> ResponseEntity.ok(newspaperService.getLinkToSingleTitle(catalogueId))
            else -> throw NotSupportedException("Material type $materialType is not supported.")
        }
    }
}
