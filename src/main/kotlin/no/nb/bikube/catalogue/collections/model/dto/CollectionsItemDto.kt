package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.model.inputDto.ItemInputDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class ItemDto (
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
    val dataset: String? = null,

    @SerialName("part_of_reference.lref")
    val partOfReference: String? = null
)

fun createNewspaperItemDto(item: ItemInputDto, manifestationCatalogueId: String): ItemDto {
    val useUrn = item.digital == true && !item.urn.isNullOrBlank()

    return ItemDto(
        format = if (item.digital == true) CollectionsFormat.DIGITAL.value else CollectionsFormat.PHYSICAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        altNumber = if (useUrn) item.urn else null,
        altNumberType = if (useUrn) "URN" else null,
        inputName = "Bikube API", // TODO: Change when we have authentication in place
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = manifestationCatalogueId
    )
}
