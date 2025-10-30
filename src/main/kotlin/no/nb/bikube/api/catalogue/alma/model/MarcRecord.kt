package no.nb.bikube.api.catalogue.alma.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText

@JacksonXmlRootElement(localName = "record")
data class MarcRecord(
    val leader: String,
    val controlfield: List<ControlField>?,
    val datafield: List<DataField>
) {
    @JacksonXmlProperty(isAttribute = true, localName = "format")
    val format: String = "MARC21"

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    val type: String = "Bibliographic"

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns")
    val marcNamespace: String = "info:lc/xmlns/marcxchange-v1"

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:xsi")
    val xsiNamespace: String = "http://www.w3.org/2001/XMLSchema-instance"

    @JacksonXmlProperty(isAttribute = true, localName = "xsi:schemaLocation")
    val xsiSchemaLocation: String = "info:lc/xmlns/marcxchange-v1 http://www.loc.gov/standards/iso25577/marcxchange-1-1.xsd"
}

data class ControlField(
    @JacksonXmlProperty(isAttribute = true)
    val tag: String,

    @JacksonXmlProperty(localName = "innerText")
    @JacksonXmlText
    val content: String?
)

data class DataField(
    @JacksonXmlProperty(isAttribute = true) val tag: String,
    @JacksonXmlProperty(isAttribute = true) val ind1: String,
    @JacksonXmlProperty(isAttribute = true) val ind2: String,
    @JacksonXmlElementWrapper(useWrapping = false) val subfield: List<SubField>
)

data class SubField(
    @JacksonXmlProperty(isAttribute = true)
    val code: String,

    @JacksonXmlProperty(localName = "innerText")
    @JacksonXmlText
    val content: String?
)
