package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import no.nb.bikube.catalogue.alma.repository.AlmaRepository
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Validated
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
        ApiResponse(responseCode = "400", description = "Invalid MMS"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun getAlmaItemByMMS(
        @Parameter(description = "Title MMS-ID")
        @Pattern(regexp = MMS_REGEX, message = MMS_MESSAGE)
        @PathVariable mms: String,
        @Parameter(description = "Include XML declaration/prolog in XML output.")
        @RequestParam(required = false, defaultValue = "true") prolog: Boolean
    ): Mono<ByteArray> {
        return almaRepository.getRecordByMMS(mms, prolog)
    }

    companion object {
        const val MMS_REGEX = "[0-9]{8,19}"
        const val MMS_MESSAGE = "MMS-ID kan kun inneholde tall, og må være mellom 8 og 19 tegn."
    }
}