package no.nb.bikube.core.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
class CollectionsModelFromJsonSingleNewspaperItemTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperItemSingle.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).adlibJson.recordList.first()

    @Test
    fun `Item object should extract priRef`() { Assertions.assertEquals("5", singleItem.priRef) }

    @Test
    fun `Item object should extract format`() { Assertions.assertEquals(AxiellFormat.DIGITAL.value, singleItem.formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text) }

    @Test
    fun `Item object should extract medium`() { Assertions.assertEquals("Tekst", singleItem.mediumList!!.first().medium) }

    @Test
    fun `Item object should extract submedium`() { Assertions.assertEquals("Avis", singleItem.subMediumList!!.first().subMedium) }

    @Test
    fun `Item object should extract title`() { Assertions.assertEquals("Bikubeavisen 2012.01.02", singleItem.titleList!!.first().title) }

    @Test
    fun `Item object should extract recordtype`() { Assertions.assertEquals(AxiellRecordType.ITEM.value, singleItem.recordTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text) }

    @Test
    fun `Item object should extract parent manifestation`() {
        val manifestation = singleItem.partOfList!!.first().partOfReference!!
        Assertions.assertEquals("10", manifestation.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", manifestation.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, manifestation.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", manifestation.subMedium!!.first().subMedium)
    }

    @Test
    fun `Item object should extract parent year work`() {
        val yearWork = singleItem.partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals("4", yearWork.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", yearWork.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, yearWork.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", yearWork.subMedium!!.first().subMedium)
        Assertions.assertEquals(AxiellDescriptionType.YEAR.value, yearWork.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
    }

    @Test
    fun `Item object should extract parent title`() {
        val title = singleItem.partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals("3", title.priRef)
        Assertions.assertEquals("Bikubeavisen", title.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, title.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", title.subMedium!!.first().subMedium)
        Assertions.assertEquals(AxiellDescriptionType.SERIAL.value, title.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
    }

}
