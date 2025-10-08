package no.nb.bikube.catalogue.alma.service

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nb.bikube.catalogue.alma.model.*
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
    fun parseBibResult(res: String): AlmaBibResult {
        return mapper.readValue(res, AlmaBibResult::class.java)
    }

    @Throws(JacksonException::class)
    fun parseItemResult(res: String): AlmaItemResult {
        return mapper.readValue(res, AlmaItemResult::class.java)
    }

    @Throws(JacksonException::class)
    fun parseSruResult(res: String): AlmaSruResult {
        return mapper.readValue(res, AlmaSruResult::class.java)
    }

    @Throws(JacksonException::class)
    fun writeAsByteArray(record: MarcRecord, prolog: Boolean): ByteArray {
        return (mapper as XmlMapper)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, prolog)
            .writeValueAsBytes(record)
    }

    @Throws(JacksonException::class)
    fun writeAsByteArray(recordList: RecordList): ByteArray {
        return mapper.writeValueAsBytes(recordList)
    }

    fun addEnumChron(data: ItemData, record: MarcRecord): MarcRecord {
        val enumChronMap = data.asMap()
        if (enumChronMap.isEmpty())
            return record
        val captionDataField = DataField(
            tag = "853", ind1 = "3", ind2 = "0",
            subfield = enumChronMap.keys.map {
                SubField(code = it.code, content = it.caption)
            }
        )
        val valueDataField = DataField(
            tag = "863", ind1 = " ", ind2 = " ",
            subfield = enumChronMap.entries.map {
                SubField(code = it.key.code, content = it.value)
            }
        )
        return record.copy(
            datafield = (record.datafield + captionDataField + valueDataField)
                .sortedBy { it.tag }
        )
    }

    @Throws(JacksonException::class)
    fun parseErrorResponse(res: String): AlmaErrorResponse {
        return mapper.readValue(res, AlmaErrorResponse::class.java)
    }
}
