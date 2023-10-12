package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.AxiellNameType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class AxiellNameRecordDto(
    val name: String,

    @SerialName("name.type")
    val nameType: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("priref")
    val priRef: String? = null
)

fun createNameRecordDtoFromString(name: String): AxiellNameRecordDto {
    return AxiellNameRecordDto(
        name = name,
        nameType = AxiellNameType.PUBLISHER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        inputName = "Bikube API" // TODO: Change when we have authentication in place
    )
}