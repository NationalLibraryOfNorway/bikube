package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.Item
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val altNumberType: String?,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("input.source")
    val inputSource: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("dataset_name")
    val dataset: String? = null
)

fun createNewspaperItemDto(item: Item): ItemDto {
    return ItemDto(
        name = item.name,
        format = if (item.digital == true) AxiellFormat.DIGITAL.value else AxiellFormat.PHYSICAL.value,
        recordType = AxiellRecordType.ITEM.value,
        altNumber = item.urn,
        altNumberType = if (!item.urn.isNullOrBlank()) "URN" else null,
        inputName = "Bikube API", // TODO: Change when we have authentication in place
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    )
}
