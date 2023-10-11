package no.nb.bikube.core.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TitleDto(
    val title: String,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("work.description_type")
    val descriptionType: String?,

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

    @SerialName("place_of_publication.lref")
    val placeOfPublicationRef: String? = null,

    @SerialName("language")
    val language: String? = null
)