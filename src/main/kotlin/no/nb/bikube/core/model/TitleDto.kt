package no.nb.bikube.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TitleDto(
    val title: String,
    @SerialName("record_type") val recordType: String?,
    @SerialName("work.description_type") val descriptionType: String?,
    @SerialName("submedium") val subMedium: String? = null
)