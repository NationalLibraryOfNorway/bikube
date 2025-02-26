package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsTermType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class CollectionsTermRecordDto(
    @SerialName("term")
    val term: String,

    @SerialName("term.type")
    val termType: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("input.source")
    val inputSource: String? = null,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("input.notes")
    val inputNotes: String? = null,

    @SerialName("priref")
    val priRef: String? = null
)

fun createTermRecordDtoFromString(
    termName: String,
    termType: CollectionsTermType,
    username: String
): CollectionsTermRecordDto {
    return CollectionsTermRecordDto(
        term = termName,
        termType = termType.value,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        inputName = username,
        inputNotes = "Registrert i Bikube API"
    )
}
