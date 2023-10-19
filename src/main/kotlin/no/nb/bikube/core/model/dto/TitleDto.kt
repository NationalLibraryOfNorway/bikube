package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.TitleInputDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class TitleDto(
    val title: String,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("work.description_type")
    val descriptionType: String?,

    @SerialName("medium")
    val medium: String? = null,

    @SerialName("submedium")
    val subMedium: String? = null,

    @SerialName("dating.date.start")
    val dateStart: String? = null,

    @SerialName("dating.date.end")
    val dateEnd: String? = null,

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

    @SerialName("dataset_name")
    val dataset: String? = null
)

fun createNewspaperTitleDto(title: TitleInputDto): TitleDto {
    return TitleDto(
        title = title.name!!,
        dateStart = title.startDate?.toString(),
        dateEnd = title.endDate?.toString(),
        publisher = title.publisher,
        placeOfPublication = title.publisherPlace,
        language = title.language,
        recordType = AxiellRecordType.WORK.value,
        descriptionType = AxiellDescriptionType.SERIAL.value,
        medium = "Tekst",
        subMedium = MaterialType.NEWSPAPER.norwegian,
        inputName = "Bikube API", // TODO: Change when we have authentication in place
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    )
}
