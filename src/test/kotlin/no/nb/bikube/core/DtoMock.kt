package no.nb.bikube.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DtoMock(
    @SerialName("work.description_type")
    val descriptionType: String? = null,

    @SerialName("record_type")
    val recordType: String? = null
)
