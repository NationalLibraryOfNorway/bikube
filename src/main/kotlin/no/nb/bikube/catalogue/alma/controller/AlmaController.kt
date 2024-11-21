package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Pattern
import no.nb.bikube.catalogue.alma.enum.OtherField
import no.nb.bikube.catalogue.alma.model.MarcRecord
import no.nb.bikube.catalogue.alma.service.AlmaService
import no.nb.bikube.catalogue.alma.service.AlmaSruService
import no.nb.bikube.catalogue.alma.service.MarcXChangeService
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
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

    @GetMapping("/isbn/{isbn}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic records by ISBN.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Invalid ISBN", content = [Content()]),
        ApiResponse(responseCode = "404", description = "ISBN not found", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun getMarcRecordsByISBN(
        @Parameter(description = "ISBN")
        @Pattern(regexp = ISBN_REGEX, message = ISBN_MESSAGE)
        @PathVariable isbn: String
    ): Mono<ByteArray> {
        return almaSruService.getRecordsByISBN(isbn)
            .map { marcXChangeService.writeAsByteArray(it) }
    }

    @GetMapping("/ismn/{ismn}", produces = [MediaType.APPLICATION_XML_VALUE])
    @Operation(summary = "Get bibliographic records by ISMN.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "400", description = "Invalid ISMN", content = [Content()]),
        ApiResponse(responseCode = "404", description = "ISMN not found", content = [Content()]),
        ApiResponse(responseCode = "500", description = "Server error", content = [Content()])
    ])
    fun getMarcRecordsByISMN(
        @Parameter(description = "ISMN")
        @Pattern(regexp = ISMN_REGEX, message = ISMN_MESSAGE)
        @PathVariable ismn: String
    ): Mono<ByteArray> {
        return almaSruService.getRecordsByISMN(ismn)
            .map { marcXChangeService.writeAsByteArray(it) }
    }

    @GetMapping("/html/barcode/{barcode}", produces = [MediaType.TEXT_HTML_VALUE])
    fun getHTMLBarcode(
        @Parameter(description = "Item barcode")
        @Pattern(regexp = BARCODE_REGEX, message = BARCODE_MESSAGE)
        @PathVariable barcode: String
    ): Mono<ModelAndView> {
        val marcRecord = almaService.getRecordByBarcode(barcode)
        val modelAndView = ModelAndView()
        modelAndView.viewName = "check"
        modelAndView.addObject("id", "strekkode: $barcode")
        addMetadata(marcRecord, modelAndView)
        return Mono.just(modelAndView)
    }

    companion object {
        const val MMS_REGEX = "[0-9]{8,19}"
        const val MMS_MESSAGE = "MMS-ID kan kun inneholde tall, og må være mellom 8 og 19 tegn."
        const val BARCODE_REGEX = "[a-zA-Z0-9]{4,13}"
        const val BARCODE_MESSAGE = "Strekkode kan kun inneholde tall og bokstaver, og må være mellom 4 og 13 tegn."
        const val ISSN_REGEX = "[0-9]{4}-?[0-9]{3}[0-9Xx]"
        const val ISSN_MESSAGE = "Invalid ISSN pattern"
        const val ISBN_REGEX = "([0-9]-?){9}[0-9](-?(([0-9]-?){2}[0-9]))?"
        const val ISBN_MESSAGE = "Invalid ISBN pattern"
        const val ISMN_REGEX = "$ISBN_REGEX|M-?[0-9]{4}-?[0-9]{4}-?[0-9]"
        const val ISMN_MESSAGE = "Invalid ISMN pattern"
        private fun addMetadata(marcRecord: Mono<MarcRecord>, modelAndView: ModelAndView) {
            marcRecord.subscribe { record ->
                modelAndView.addObject("titleObject", record.datafield.find { it.tag == OtherField.TITLE.tag }?.subfield?.find { it.code == "a" }?.content.orEmpty())
                modelAndView.addObject("authorObject", record.datafield.find { it.tag == OtherField.AUTHOR.tag }?.subfield?.find { it.code == "a" }?.content.orEmpty())
                modelAndView.addObject("yearObject", record.datafield.find { it.tag == OtherField.YEAR.tag }?.subfield?.find { it.code == "a" }?.content.orEmpty())
                modelAndView.addObject("pagesObject", record.datafield.find { it.tag == OtherField.NUMBER_OF_PAGES.tag }?.subfield?.find { it.code == "a" }?.content.orEmpty())
            }
        }
    }
}
