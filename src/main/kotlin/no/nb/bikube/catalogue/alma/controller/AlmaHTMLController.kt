package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.catalogue.alma.enum.OtherField
import no.nb.bikube.catalogue.alma.service.AlmaService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Controller for handling requests for bibliographic records in HTML format.
 */
@Controller
@Tag(
    name = "MarcXchange HTML",
    description = "Get bibliographic records as MarcXchange encoded MARC-21 in HTML"
)
@RequestMapping("/alma/html")
class AlmaHTMLController(
    private val almaService: AlmaService
) {

    @RequestMapping("/barcode/{barcode}")
    fun getHTMLByBarcode(
        model: Model,
        @PathVariable barcode: String
    ): String {
        val metadata = almaService.getRecordByBarcode(barcode)

        metadata.mapNotNull { md -> md.datafield.find { df ->
            df.tag == OtherField.TITLE.tag
        }?.subfield?.find { sf -> sf.code == OtherField.TITLE.code }?.content }.let {
            model.addAttribute("titleObject", it)
        }
        metadata.mapNotNull { md -> md.datafield.find { df ->
            df.tag == OtherField.AUTHOR.tag
        }?.subfield?.find { sf -> sf.code == OtherField.AUTHOR.code }?.content }.let {
            model.addAttribute("authorObject", it)
        }
        metadata.mapNotNull { md -> md.datafield.find { df ->
            df.tag == OtherField.YEAR.tag
        }?.subfield?.find { sf -> sf.code == OtherField.YEAR.code }?.content }.let {
            model.addAttribute("yearObject", it)
        }
        metadata.mapNotNull { md -> md.datafield.find { df ->
            df.tag == OtherField.NUMBER_OF_PAGES.tag
        }?.subfield?.find { sf -> sf.code == OtherField.NUMBER_OF_PAGES.code }?.content }.let {
            model.addAttribute("numberOfPagesObject", it)
        }

        return "marc_record"
    }
}