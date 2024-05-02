package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class ManifestationDto (
    @SerialName("priref")
    val priRef: String? = null,

    @SerialName("object_number")
    val objectNumber: String? = null,

    @SerialName("part_of_reference.lref")
    val partOfReference: String?,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("dating.date.start")
    val dateStart: String? = null,

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
    val dataset: String? = null
)

fun createManifestationDto(
    id: String,
    parentCatalogueId: String,
    date: LocalDate,
    username: String
): ManifestationDto {
    return ManifestationDto(
        priRef = id,
        objectNumber = "TE-$id",
        partOfReference = parentCatalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        dateStart = date.toString(),
        inputName = username,
        inputNotes = "Registrert i Bikube",
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    )
}
