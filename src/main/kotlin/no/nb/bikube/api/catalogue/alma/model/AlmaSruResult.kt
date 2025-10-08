package no.nb.bikube.api.catalogue.alma.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "searchRetrieveResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
data class AlmaSruResult(
    val numberOfRecords: Int,
    @JacksonXmlElementWrapper(localName="records") val records: List<RecordItem>
)

@JacksonXmlRootElement(localName = "record")
@JsonIgnoreProperties(ignoreUnknown = true)
data class RecordItem(
    val recordData: RecordData
)

@JacksonXmlRootElement(localName = "recordData")
data class RecordData(
    val record: MarcRecord
)
