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
    val priRef: String,

    @SerialName("object_number")
    val objectNumber: String,

    @SerialName("part_of_reference.lref")
    val partOfReference: String?,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("edition.date")
    val date: String? = null,

    @SerialName("edition")
    val edition: String? = null,

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

    @SerialName("notes")
    val notes: String? = null,

    @SerialName("Alternative_number")
    val alternativeNumbers: List<AlternativeNumberInput>? = null,
)

fun createManifestationDto(
    id: String,
    parentCatalogueId: String,
    date: LocalDate,
    username: String,
    notes: String? = null,
    number: String? = null
): ManifestationDto {
    val altNumbers = number?.let { listOf(AlternativeNumberInput(it, "Nummer")) }

    return ManifestationDto(
        priRef = id,
        objectNumber = "TE-$id",
        partOfReference = parentCatalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        date = date.toString(),
        edition = number,
        inputName = username,
        inputNotes = "Registrert i Bikube API",
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        notes = notes,
        alternativeNumbers = altNumbers
    )
}
