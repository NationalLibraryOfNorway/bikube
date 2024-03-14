package no.nb.bikube.catalogue.alma.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Service
import org.w3c.dom.Document

@Service
class DocumentMapper {

    val mapper = XmlMapper.Builder(
        XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
            setXMLTextElementName("innerText")
        })
    )
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build()
        .registerKotlinModule()

    fun parseDocument(doc: ByteArray): Document {
        return mapper.readValue<Document>(doc)
    }

}
