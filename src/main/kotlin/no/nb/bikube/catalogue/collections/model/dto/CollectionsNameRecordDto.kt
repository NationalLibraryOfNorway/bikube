package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsNameType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class CollectionsNameRecordDto(
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

fun createNameRecordDtoFromString(name: String): CollectionsNameRecordDto {
    return CollectionsNameRecordDto(
        name = name,
        nameType = CollectionsNameType.PUBLISHER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        inputName = "Bikube API" // TODO: Change when we have authentication in place
    )
}
