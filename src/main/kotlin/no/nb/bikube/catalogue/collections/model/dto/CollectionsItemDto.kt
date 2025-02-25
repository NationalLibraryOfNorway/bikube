package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsItemStatus
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.model.inputDto.ItemInputDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class ItemDto (
    @SerialName("priref")
    val priRef: String,

    @SerialName("object_number")
    val objectNumber: String,

    @SerialName("format")
    val format: String,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("input.source")
    val inputSource: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("input.notes")
    val inputNotes: String? = null,

    @SerialName("dataset_name")
    val dataset: String? = null,

    @SerialName("part_of_reference.lref")
    val partOfReference: String? = null,

    @SerialName("Alternative_number")
    val alternativeNumberList: List<AlternativeNumberInput>? = null,

    @SerialName("PID_data_URN")
    val urn: String? = null,

    @SerialName("item_status")
    val itemStatus: String? = null,

    @SerialName("current_location.name")
    val currentLocationName: String? = null,
)

@Serializable
data class AlternativeNumberInput (
    @SerialName("alternative_number")
    val name: String,

    @SerialName("alternative_number.type")
    val type: String
)

fun createNewspaperItemDto(
    id: String,
    item: ItemInputDto,
    manifestationCatalogueId: String
): ItemDto {
    val useUrn = item.digital == true && !item.urn.isNullOrBlank()

    return ItemDto(
        priRef = id,
        objectNumber = "TE-$id",
        format = if (item.digital == true) CollectionsFormat.DIGITAL.value else CollectionsFormat.PHYSICAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        inputName = item.username,
        inputNotes = "Registrert i Bikube API",
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = manifestationCatalogueId,
        alternativeNumberList = if (useUrn) listOf(AlternativeNumberInput(item.urn!!, "URN")) else null,
        urn = if (useUrn) item.urn else null,
        itemStatus = if (item.digital == true) CollectionsItemStatus.fromString(item.itemStatus)?.value else null,
        currentLocationName = if (item.digital == false) item.containerId else null
    )
}
