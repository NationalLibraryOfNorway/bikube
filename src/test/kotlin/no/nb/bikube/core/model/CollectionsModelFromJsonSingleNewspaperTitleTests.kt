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
class CollectionsModelFromJsonSingleNewspaperTitleTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleTitleJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleSingle.json")
    private val singleTitle = mapper().readValue<CollectionsModel>(singleTitleJson).adlibJson.recordList.first()

    @Test
    fun `Title object should extract priRef`() { Assertions.assertEquals("3", singleTitle.priRef) }

    @Test
    fun `Title object should extract start date`() { Assertions.assertEquals("1947-01-01", singleTitle.datingList!!.first().dateFrom) }

    @Test
    fun `Title object should extract end date`() { Assertions.assertEquals("1999-01-09", singleTitle.datingList!!.first().dateTo) }

    @Test
    fun `Title object should extract language`() { Assertions.assertEquals("nob", singleTitle.languageList!!.first().language) }

    @Test
    fun `Title object should extract submedium`() { Assertions.assertEquals("Avis", singleTitle.subMediumList!!.first().subMedium) }

    @Test
    fun `Title object should extract title`() { Assertions.assertEquals("Bikubeavisen", singleTitle.titleList!!.first().title) }

    @Test
    fun `Title object should extract publication place `() { Assertions.assertEquals("Mo i Rana", singleTitle.placeOfPublicationList!!.first()) }

    @Test
    fun `Title object should extract publisher`() { Assertions.assertEquals("Amedia", singleTitle.publisherList!!.first()) }

    @Test
    fun `Title object should extract work type`() {
        Assertions.assertEquals(
            AxiellDescriptionType.SERIAL.value,
            singleTitle.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text
        )
    }

    @Test
    fun `Title object should extract child year works`() {
        val yearWorks = singleTitle.partsList!!
        Assertions.assertEquals(2, yearWorks.size)

        val firstYear = yearWorks.first().partsReference!!
        Assertions.assertEquals("11", firstYear.priRef)
        Assertions.assertEquals("Bikubeavisen 2011", firstYear.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, firstYear.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellDescriptionType.YEAR.value, firstYear.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(1, firstYear.partsList!!.size)

        val secondYear = yearWorks[1].partsReference!!
        Assertions.assertEquals("4", secondYear.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", secondYear.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.WORK.value, secondYear.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellDescriptionType.YEAR.value, secondYear.workTypeList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(3, secondYear.partsList!!.size)
    }

    @Test
    fun `Title object should extract child manifestations`() {
        val manifestations = mutableListOf<CollectionsPartsObject>()
        singleTitle.partsList!!.forEach { yearWorks -> yearWorks.partsReference!!.partsList?.forEach { manifestations.add(it) } }
        Assertions.assertEquals(4, manifestations.size)

        val manifest1 = manifestations.first().partsReference!!
        Assertions.assertEquals("18", manifest1.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", manifest1.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, manifest1.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(1, manifest1.partsList!!.size)

        val manifest2 = manifestations[1].partsReference!!
        Assertions.assertEquals("22", manifest2.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.10", manifest2.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, manifest2.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(null, manifest2.partsList)

        val manifest3 = manifestations[2].partsReference!!
        Assertions.assertEquals("20", manifest3.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", manifest3.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, manifest3.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(1, manifest3.partsList!!.size)

        val manifest4 = manifestations[3].partsReference!!
        Assertions.assertEquals("10", manifest4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", manifest4.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION.value, manifest4.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(2, manifest4.partsList!!.size)
    }

    @Test
    fun `Title object should extract child items`() {
        val items = mutableListOf<CollectionsPartsObject>()
        singleTitle.partsList!!.forEach { yearWorks ->
            yearWorks.partsReference!!.partsList?.forEach { manifestations ->
                manifestations.partsReference!!.partsList?.forEach { items.add(it) }
            }
        }
        Assertions.assertEquals(4, items.size)

        val item1 = items.first().partsReference!!
        Assertions.assertEquals("19", item1.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", item1.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM.value, item1.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.PHYSICAL.value, item1.formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        val item2 = items[1].partsReference!!
        Assertions.assertEquals("21", item2.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", item2.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM.value, item2.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.DIGITAL.value, item2.formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        val item3 = items[2].partsReference!!
        Assertions.assertEquals("6", item3.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", item3.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM.value, item3.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.PHYSICAL.value, item3.formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)

        val item4 = items[3].partsReference!!
        Assertions.assertEquals("5", item4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", item4.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM.value, item4.recordType!!.first().first { langObj -> langObj.lang == "neutral" }.text)
        Assertions.assertEquals(AxiellFormat.DIGITAL.value, item4.formatList!!.first().first { langObj -> langObj.lang == "neutral" }.text)
    }

}
