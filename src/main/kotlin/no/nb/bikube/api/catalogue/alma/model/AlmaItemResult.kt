package no.nb.bikube.api.catalogue.alma.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import no.nb.bikube.api.catalogue.alma.enum.EnumChronCaption
import java.util.*

@JacksonXmlRootElement(localName = "item")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AlmaItemResult (
    val bibData: BibData,
    val itemData: ItemData
)

@JacksonXmlRootElement(localName = "bib_data")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BibData (
    val mmsId: String
)

@JacksonXmlRootElement(localName = "item_data")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ItemData (
    val enumerationA: String,
    val enumerationB: String,
    val enumerationC: String,
    val chronologyI: String,
    val chronologyJ: String,
    val chronologyK: String
) {
    fun asMap(): SortedMap<EnumChronCaption, String> {
        return mapOf(
            EnumChronCaption.A to this.enumerationA,
            EnumChronCaption.B to this.enumerationB,
            EnumChronCaption.C to this.enumerationC,
            EnumChronCaption.I to this.chronologyI,
            EnumChronCaption.J to this.chronologyJ,
            EnumChronCaption.K to this.chronologyK
        )
            .filter { it.value.isNotEmpty() }
            .toSortedMap()
    }
}
