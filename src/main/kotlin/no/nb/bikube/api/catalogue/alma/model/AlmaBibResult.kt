package no.nb.bikube.api.catalogue.alma.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "bib")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AlmaBibResult (
    val mmsId: String,
    val recordFormat: String,
    val record: MarcRecord
)
