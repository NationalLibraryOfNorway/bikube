package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.catalogue.alma.repository.AlmaRepository
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@Tag(
    name = "MarcXchange",
    description = "Get bibliographic records as MarcXchange encoded MARC-21"
)
@RequestMapping("/alma")
class AlmaController(
    private val almaRepository: AlmaRepository
) {

    @GetMapping("/mms/{mms}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic record by title MMS-ID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun getAlmaItemByMMS(
        @PathVariable mms: String,
        @RequestParam(required = false, defaultValue = "true") prolog: Boolean
    ): Mono<ByteArray> {
        return almaRepository.getRecordByMMS(mms, prolog)
    }

}