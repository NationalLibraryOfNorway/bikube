package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.Item

@Serializable
class ItemDto (
    @SerialName("title")
    val name: String?,

    @SerialName("format")
    val format: String,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("alternative_number")
    val altNumber: String?,

    @SerialName("alternative_number.type")
    val altNumberType: String?
)

fun createNewspaperItemDto(item: Item): ItemDto {
    return ItemDto(
        name = item.name,
        format = if (item.digital == true) AxiellFormat.DIGITAL.value else AxiellFormat.PHYSICAL.value,
        recordType = AxiellRecordType.ITEM.value,
        altNumber = item.urn,
        altNumberType = if (!item.urn.isNullOrBlank()) "URN" else null
    )
}
