package no.nb.bikube.api.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.api.core.model.inputDto.TitleInputDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class CollectionsSeriesDto(
    @SerialName("priref")
    val priRef: String? = null,

    @SerialName("series")
    val series: List<String>,

    @SerialName("Dating")
    val dating: List<CollectionsSeriesDatingDto>? = null,

    @SerialName("publisher")
    val publisher: String? = null,

    @SerialName("place_of_publication")
    val placeOfPublication: String? = null,

    @SerialName("language")
    val language: String? = null,

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
)

@Serializable
data class CollectionsSeriesDatingDto(
    @SerialName("date_start")
    val dateStart: String? = null,

    @SerialName("date_end")
    val dateEnd: String? = null,
)

fun createSeriesDto(
    id: String? = null,
    title: TitleInputDto,
): CollectionsSeriesDto {
    val dating = if (title.startDate != null || title.endDate != null) {
        listOf(CollectionsSeriesDatingDto(
            dateStart = title.startDate?.toString(),
            dateEnd = title.endDate?.toString()
        ))
    } else null

    return CollectionsSeriesDto(
        priRef = id,
        series = listOf(title.name),
        dating = dating,
        publisher = title.publisher,
        placeOfPublication = title.publisherPlace,
        language = title.language,
        inputName = title.username,
        inputNotes = "Registrert i Bikube API",
        inputSource = "series",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
    )
}
