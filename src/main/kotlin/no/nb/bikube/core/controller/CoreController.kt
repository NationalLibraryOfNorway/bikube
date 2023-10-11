package no.nb.bikube.core.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.enum.*
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.exception.AxiellTitleNotFound
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.model.*
import no.nb.bikube.newspaper.service.AxiellService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
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


    @GetMapping("/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Search catalogue",
        description =
            "Case insensitive search on various types (titles, publishers, items etc.) and various material types. " +
            "Supports wildcard operator * for partial matches."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun search(
        @RequestParam searchTerm: String,
        @RequestParam searchType: SearchType,
        @RequestParam materialType: MaterialType
    ): ResponseEntity<Flux<CatalogueRecord>> {
        if (searchTerm.isEmpty()) throw BadRequestBodyException("Search term cannot be empty.")

        return when (searchType) {
            SearchType.TITLE -> {
                when(materialTypeToCatalogueName(materialType)) {
                    CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.searchTitleByName(searchTerm))
                    else -> throw NotSupportedException("Material type $materialType is not supported.")
                }
            }
            SearchType.PUBLISHER -> {
                when(materialTypeToCatalogueName(materialType)) {
                    CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.searchPublisherByName(searchTerm))
                    else -> throw NotSupportedException("Material type $materialType is not supported.")
                }
            }
            SearchType.LANGUAGE -> {
                when(materialTypeToCatalogueName(materialType)) {
                    CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.searchLanguageByName(searchTerm))
                    else -> throw NotSupportedException("Material type $materialType is not supported.")
                }
            }
            SearchType.LOCATION -> {
                when(materialTypeToCatalogueName(materialType)) {
                    CatalogueName.COLLECTIONS -> ResponseEntity.ok(axiellService.searchPublisherPlaceByName(searchTerm))
                    else -> throw NotSupportedException("Material type $materialType is not supported.")
                }
            }
            else -> throw NotSupportedException("Search type $searchType is not supported.")
        }
    }

    @PostMapping("/publisher", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create new publisher")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createPublisher(
        @RequestBody publisher: String
    ): Mono<ResponseEntity<Publisher>> {
        return if (publisher.isEmpty()) {
            throw BadRequestBodyException("Publisher cannot be empty.")
        } else axiellService.createPublisher(publisher).map { created ->
            ResponseEntity.ok(created)
        }
    }

    @PostMapping("/publisher-place", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create new publisher place (location)")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createPublisherPlace(
        @RequestBody location: String
    ): Mono<ResponseEntity<PublisherPlace>> {
        return if (location.isEmpty()) {
            throw BadRequestBodyException("Publisher place (location) cannot be empty.")
        } else axiellService.createPublisherPlace(location).map { created ->
            ResponseEntity.ok(created)
        }
    }

    @PostMapping("/language", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create new ISO-639-2 language code")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Bad request"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun createLanguage(
        @RequestBody language: String
    ): Mono<ResponseEntity<Language>> {
        if (!Regex("^[a-z]{3}$").matches(language)) {
            throw BadRequestBodyException("Language code must be a valid ISO-639-2 language code.")
        }
        return axiellService.createLanguage(language).map { created ->
            ResponseEntity.ok(created)
        }
    }
}
