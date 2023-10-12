package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.Title

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
    val language: String? = null
)

fun createNewspaperTitleDto(title: Title): TitleDto {
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
        subMedium = title.materialType
    )
}