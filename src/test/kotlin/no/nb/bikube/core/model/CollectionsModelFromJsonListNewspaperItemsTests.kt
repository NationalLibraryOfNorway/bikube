package no.nb.bikube.core.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.collections.CollectionsModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
class CollectionsModelFromJsonListNewspaperItemsTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val itemListJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperItemList.json")
    private val items = mapper().readValue<CollectionsModel>(itemListJson).adlibJson.recordList!!

    @Test
    fun `Collection object should extract all items`() {
        Assertions.assertEquals(4, items.size)
    }

    @Test
    fun `Collection object should extract priRefs `() {
        Assertions.assertEquals("5", items.first().priRef)
        Assertions.assertEquals("6", items[1].priRef)
        Assertions.assertEquals("19", items[2].priRef)
        Assertions.assertEquals("21", items[3].priRef)
    }

    @Test
    fun `Collection object should extract submediums`() {
        Assertions.assertTrue(items.all { it.subMediumList!!.first().subMedium == "Avis" })
    }

    @Test
    fun `Collection object should extract names`() {
        Assertions.assertEquals("Bikubeavisen 2012.01.02", items.first().titleList!!.first().title)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", items[1].titleList!!.first().title)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", items[2].titleList!!.first().title)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", items[3].titleList!!.first().title)
    }

    @Test
    fun `Collection object should extract record types`() {
        Assertions.assertTrue(items.all { it.recordTypeList!!.first().first{ langObj -> langObj.lang == "neutral" }.text == "ITEM" })
    }

    @Test
    fun `Collection object should extract formats`() {
        Assertions.assertEquals(AxiellFormat.DIGITAL.value, items.first().formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.PHYSICAL.value, items[1].formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.PHYSICAL.value, items[2].formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.DIGITAL.value, items[3].formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
    }

    @Test
    fun `Collection object should extract parent manifestations`() {
        val mani1 = items.first().partOfList!!.first().partOfReference!!
        Assertions.assertEquals("10", mani1.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", mani1.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, mani1.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", mani1.subMedium!!.first().subMedium)

        // Manifestation 1 and 2 should be the same
        val mani2 = items[1].partOfList!!.first().partOfReference!!
        Assertions.assertEquals(mani1, mani2)

        val mani3 = items[2].partOfList!!.first().partOfReference!!
        Assertions.assertEquals("18", mani3.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", mani3.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, mani3.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", mani3.subMedium!!.first().subMedium)

        val mani4 = items[3].partOfList!!.first().partOfReference!!
        Assertions.assertEquals("20", mani4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", mani4.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, mani4.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", mani4.subMedium!!.first().subMedium)
    }

    @Test
    fun `Collection object should extract parent year works`() {
        val yearWork1 = items.first().partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals("4", yearWork1.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", yearWork1.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, yearWork1.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", yearWork1.subMedium!!.first().subMedium)
        Assertions.assertEquals(AxiellDescriptionType.YEAR.value, yearWork1.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        // Year work 1, 2 and 4 should be the same
        val yearWork2 = items[1].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals(yearWork1, yearWork2)

        val yearWork3 = items[2].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals("11", yearWork3.priRef)
        Assertions.assertEquals("Bikubeavisen 2011", yearWork3.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, yearWork3.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", yearWork3.subMedium!!.first().subMedium)
        Assertions.assertEquals(AxiellDescriptionType.YEAR.value, yearWork3.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        val yearWork4 = items[3].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals(yearWork1, yearWork4)
    }

    @Test
    fun `Collection object should extract parent serial works`() {
        // All items are connected to the same title
        val title1 = items.first().partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals("3", title1.priRef)
        Assertions.assertEquals("Bikubeavisen", title1.title!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, title1.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals("Avis", title1.subMedium!!.first().subMedium)
        Assertions.assertEquals(AxiellDescriptionType.SERIAL.value, title1.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        val title2 = items[1].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals(title1, title2)

        val title3 = items[2].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals(title1, title3)

        val title4 = items[3].partOfList!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!.partOfGroup!!.first().partOfReference!!
        Assertions.assertEquals(title1, title4)
    }
}
