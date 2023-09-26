package no.nb.bikube.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonObject

@Serializable
class TitleDto(
    val title: String,
    @SerialName("record_type") val recordType: String?,
    @SerialName("work.description_type") val descriptionType: String?,
    @SerialName("submedium") val subMedium: String? = null
)