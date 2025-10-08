package no.nb.bikube.api.catalogue.alma.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "records")
data class RecordList(
    val record: List<MarcRecord>
)
