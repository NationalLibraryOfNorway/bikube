package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.catalogue.collections.enum.CollectionsDescriptionType
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.model.inputDto.TitleInputDto
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

    @SerialName("input.notes")
    val inputNotes: String? = null,

    @SerialName("dataset_name")
    val dataset: String? = null
)

fun createNewspaperTitleDto(title: TitleInputDto): TitleDto {
    return TitleDto(
        title = title.name,
        dateStart = title.startDate?.toString(),
        dateEnd = title.endDate?.toString(),
        publisher = title.publisher,
        placeOfPublication = title.publisherPlace,
        language = title.language,
        recordType = CollectionsRecordType.WORK.value,
        descriptionType = CollectionsDescriptionType.SERIAL.value,
        medium = "Tekst",
        subMedium = "Aviser",
        inputName = title.username,
        inputNotes = "Registrert i Bikube",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    )
}
