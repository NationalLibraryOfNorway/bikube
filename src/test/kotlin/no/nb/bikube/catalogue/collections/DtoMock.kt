package no.nb.bikube.catalogue.collections

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class DtoMock(
    @SerialName("record_type")
    val recordType: String? = null
)
