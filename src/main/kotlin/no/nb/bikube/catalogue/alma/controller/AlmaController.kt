package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import no.nb.bikube.catalogue.alma.service.*
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
    private val almaService: AlmaService,
    private val almaSruService: AlmaSruService,
    private val marcXChangeService: MarcXChangeService
) {

    @GetMapping("/mms/{mms}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic record by title MMS-ID.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Invalid MMS"),
        ApiResponse(responseCode = "404", description = "MMS not found"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun getAlmaItemByMMS(
        @Parameter(description = "Title MMS-ID")
        @Pattern(regexp = MMS_REGEX, message = MMS_MESSAGE)
        @PathVariable mms: String,
        @Parameter(description = "Include XML declaration/prolog in XML output.")
        @RequestParam(required = false, defaultValue = "true") prolog: Boolean
    ): Mono<ByteArray> {
        return almaService.getRecordByMMS(mms)
            .map { marcXChangeService.writeAsByteArray(it.record, prolog) }
    }

    @GetMapping("/barcode/{barcode}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic record by item barcode.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Invalid barcode"),
        ApiResponse(responseCode = "404", description = "Barcode not found"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun getAlmaItemByBarcode(
        @Parameter(description = "Item barcode")
        @Pattern(regexp = BARCODE_REGEX, message = BARCODE_MESSAGE)
        @PathVariable barcode: String,
        @Parameter(description = "Include XML declaration/prolog in XML output.")
        @RequestParam(required = false, defaultValue = "true") prolog: Boolean
    ): Mono<ByteArray> {
        return almaService.getRecordByBarcode(barcode)
            .map { marcXChangeService.writeAsByteArray(it, prolog) }
    }

    @GetMapping("/issn/{issn}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic records by ISSN.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Invalid ISSN"),
        ApiResponse(responseCode = "404", description = "ISSN not found"),
        ApiResponse(responseCode = "500", description = "Server error")
    ])
    fun getMarcRecordsByISSN(
        @Parameter(description = "ISSN")
        @Pattern(regexp = ISSN_REGEX, message = ISSN_MESSAGE)
        @PathVariable issn: String
    ): Mono<ByteArray> {
        return almaSruService.getRecordsByISSN(issn)
            .map { marcXChangeService.writeAsByteArray(it) }
    }

    companion object {
        const val MMS_REGEX = "[0-9]{8,19}"
        const val MMS_MESSAGE = "MMS-ID kan kun inneholde tall, og må være mellom 8 og 19 tegn."
        const val BARCODE_REGEX = "[a-zA-Z0-9]{4,13}"
        const val BARCODE_MESSAGE = "Strekkode kan kun inneholde tall og bokstaver, og må være mellom 4 og 13 tegn."
        const val ISSN_REGEX = "[0-9]{4}-?[0-9]{3}[0-9Xx]"
        const val ISSN_MESSAGE = "Invalid ISSN pattern"
    }
}
