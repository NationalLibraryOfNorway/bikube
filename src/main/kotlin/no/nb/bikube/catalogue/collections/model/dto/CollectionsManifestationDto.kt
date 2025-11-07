package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class ManifestationDto (
    @SerialName("priref")
    val priRef: String,

    @SerialName("Title")
    val title: List<CollectionsTitleDto>? = null,

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
    objectNumber: String,
    parentCatalogueId: String,
    database: CollectionsDatabase,
    date: LocalDate,
    username: String,
    notes: String? = null,
    argang: String? = null,
    avisnr: String? = null,
    versjon: String? = null
): ManifestationDto {
    val edition = listOfNotNull(
        argang?.takeIf { it.isNotBlank() } ?: "U",
        avisnr?.takeIf { it.isNotBlank() } ?: "U",
        versjon?.takeIf { it.isNotBlank() } ?: "U"
    ).joinToString("-")
    return ManifestationDto(
        priRef = id,
        objectNumber = objectNumber,
        partOfReference = parentCatalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        date = date.toString(),
        edition = edition,
        inputName = username,
        inputNotes = "Registrert i Bikube API",
        inputSource = database.value,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = database.value,
        notes = notes,
        alternativeNumbers = listOfNotNull(
            argang?.let { AlternativeNumberInput(it, "Årgang") },
            avisnr?.let { AlternativeNumberInput(it, "Avisnr") },
            versjon?.let { AlternativeNumberInput(it, "Versjon") },
        )
    )
}
