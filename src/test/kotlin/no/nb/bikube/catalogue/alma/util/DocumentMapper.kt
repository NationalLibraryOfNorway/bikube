package no.nb.bikube.catalogue.alma.util

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Service
import org.w3c.dom.Document

@Service
class DocumentMapper {

    val mapper = XmlMapper.Builder(XmlMapper())
        .build()
        .registerKotlinModule()

    fun parseDocument(doc: ByteArray): Document {
        return mapper.readValue<Document>(doc)
    }

}
