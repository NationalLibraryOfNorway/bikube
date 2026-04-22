package no.nb.bikube.api.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Serializable
class CollectionsLocationDto(
    @SerialName("name")
    val name: String?,

    @SerialName("barcode")
    val barcode: String?,

    @SerialName("part_of")
    val partOf: String? = "S2",

    // "PACKAGE" or "LOCATION"
    @SerialName("package_location")
    val packageLocation: String? = null,

    @SerialName("input.name")
    val inputName: String?,

    @SerialName("input.date")
    val inputDate: String?,

    @SerialName("input.time")
    val inputTime: String?,

    @SerialName("input.notes")
    val inputNotes: String?,

    @SerialName("description")
    val description: String? = null
)

fun createContainerDto(
    barcode: String,
    username: String,
    location: String?,
): CollectionsLocationDto {
    return CollectionsLocationDto(
        name = barcode,
        barcode = barcode,
        partOf = location ?: "S2",
        packageLocation = "PACKAGE",
        inputName = username,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        inputNotes = "Registrert i Bikube API",
        description = null
    )
}
