package no.nb.bikube.catalogue.alma.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nb.bikube.catalogue.alma.enum.OtherField
import no.nb.bikube.catalogue.alma.model.MarcRecord
import no.nb.bikube.catalogue.alma.service.AlmaService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono
import java.util.*

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
    ): Mono<String> {
        return almaService.getRecordByBarcode(barcode).map {
            model.addAttribute("id", barcode)
            OtherField.entries.forEach { field ->
                mapMetadata(it, field)?.let { value ->
                    model.addAttribute(field.name.lowercase(Locale.getDefault()), value)
                }
            }
            "marc_record"
        }.onErrorResume { e ->
            model.addAttribute("error", e.message)
            Mono.just("error")
        }
    }


    companion object {
        fun mapMetadata(metadata: MarcRecord, otherField: OtherField): String? {
            return metadata.datafield.find { df ->
                df.tag == otherField.tag
            }?.subfield?.find { sf -> sf.code == otherField.code }?.content
        }
    }
}