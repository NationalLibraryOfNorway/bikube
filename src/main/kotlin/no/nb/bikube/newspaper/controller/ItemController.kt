package no.nb.bikube.newspaper.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.core.service.DtoValidationService
import no.nb.bikube.core.util.logger
import no.nb.bikube.newspaper.service.NewspaperService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Tag(name="Newspaper items", description="Endpoints related to newspaper items.")
@RequestMapping("/newspapers/items")
class ItemController (
    private val newspaperService: NewspaperService,
    private val dtoValidationService: DtoValidationService
){
    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a single newspaper item")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Newspaper created"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun createItem(
        @RequestBody item: ItemInputDto
    ): Mono<ResponseEntity<Item>> {
        logger().info("Trying to create newspaper item: $item")
        dtoValidationService.validateItemInputDto(item)

        // Checks that title exists before creating item. Will throw exception if not found.
        return newspaperService.getSingleTitle(item.titleCatalogueId)
            .flatMap { newspaperService.createNewspaperItem(item) }
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
            .doOnSuccess { responseEntity ->
                logger().info("Newspaper item created with id: ${responseEntity.body?.catalogueId}")
            }
    }

    @PostMapping("/missing", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create a 'missing item', in other words create a manifestation without item")
    @ApiResponses(value = [
        ApiResponse(responseCode = "201", description = "Manifestation/missing item created"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun createManifestationOnly(
        @RequestBody item: MissingPeriodicalItemDto
    ): Mono<ResponseEntity<Item>> {
        logger().info("Trying to create manifestation as missing item: $item")

        return newspaperService.createMissingItem(item)
            .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }
            .doOnSuccess { responseEntity ->
                logger().info("Manifestation created with id: ${responseEntity.body?.catalogueId}")
            }
    }

    @PutMapping("")
    @Operation(
        summary = "Update a single physical newspaper by manifestation ID.",
        description = "Update newspaper. All fields are optional (except ID and username), and fields not provided will not be updated. " +
                      "All possible fields are only present on the manifestation in the current catalog, and will be updated there."
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Newspaper item updated"),
        ApiResponse(responseCode = "400", description = "Bad request", content = [Content()]),
        ApiResponse(responseCode = "404", description = "Not found", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun updateItem(
        @RequestBody item: ItemUpdateDto
    ): Mono<ResponseEntity<Void>> {
        logger().info("Trying to update newspaper item: $item")
        dtoValidationService.validateItemUpdateDto(item)

        return newspaperService.updatePhysicalNewspaper(item)
            .map {
                logger().info("Newspaper item updated with id: ${item.manifestationId}")
                ResponseEntity.noContent().build()
            }
    }
}
