package no.nb.bikube.catalogue.alma.service

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nb.bikube.catalogue.alma.model.AlmaBibResult
import no.nb.bikube.catalogue.alma.model.AlmaErrorResponse
import org.springframework.stereotype.Service

@Service
class MarcXChangeService {

    private val mapper = XmlMapper.Builder(
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

    @Throws(JacksonException::class)
    fun parseBibResult(res: String, prolog: Boolean): ByteArray {
        val bib = mapper.readValue(res, AlmaBibResult::class.java)
        return (mapper as XmlMapper)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, prolog)
            .writeValueAsBytes(bib.record)
    }

    @Throws(JacksonException::class)
    fun parseErrorResponse(res: String): AlmaErrorResponse {
        return mapper.readValue(res, AlmaErrorResponse::class.java)
    }
}
