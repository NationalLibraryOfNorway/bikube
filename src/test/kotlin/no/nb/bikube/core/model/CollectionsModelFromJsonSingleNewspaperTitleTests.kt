package no.nb.bikube.core.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.collections.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate

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
    private val singleTitle = mapper().readValue<CollectionsModel>(singleTitleJson).getFirstObject()!!

    @Test
    fun `Title object should extract priRef`() { Assertions.assertEquals("3", singleTitle.priRef) }

    @Test
    fun `Title object should extract start date`() { Assertions.assertEquals(LocalDate.parse("1947-01-01"), singleTitle.getStartDate()) }

    @Test
    fun `Title object should extract end date`() { Assertions.assertEquals(LocalDate.parse("1999-01-09"), singleTitle.getEndDate()) }

    @Test
    fun `Title object should extract language`() { Assertions.assertEquals("nob", singleTitle.getLanguage()) }

    @Test
    fun `Title object should extract submedium`() {
        Assertions.assertEquals(MaterialType.NEWSPAPER, singleTitle.getMaterialType())
    }

    @Test
    fun `Title object should extract title`() { Assertions.assertEquals("Bikubeavisen", singleTitle.getName()) }

    @Test
    fun `Title object should extract publication place `() { Assertions.assertEquals("Mo i Rana", singleTitle.getPublisherPlace()) }

    @Test
    fun `Title object should extract publisher`() { Assertions.assertEquals("Amedia", singleTitle.getPublisher()) }

    @Test
    fun `Title object should extract work type`() {
        Assertions.assertEquals(AxiellDescriptionType.SERIAL, singleTitle.getWorkType())
    }

    @Test
    fun `Title object should extract child year works`() {
        val yearWorks = singleTitle.partsList!!
        Assertions.assertEquals(2, yearWorks.size)

        val firstYear = yearWorks.first().partsReference!!
        Assertions.assertEquals("11", firstYear.priRef)
        Assertions.assertEquals("Bikubeavisen 2011", firstYear.getName())
        Assertions.assertEquals(AxiellRecordType.WORK, firstYear.getRecordType())
        Assertions.assertEquals(AxiellDescriptionType.YEAR, firstYear.getWorkType())
        Assertions.assertEquals(1, firstYear.partsList!!.size)

        val secondYear = yearWorks[1].partsReference!!
        Assertions.assertEquals("4", secondYear.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", secondYear.getName())
        Assertions.assertEquals(AxiellRecordType.WORK, secondYear.getRecordType())
        Assertions.assertEquals(AxiellDescriptionType.YEAR, secondYear.getWorkType())
        Assertions.assertEquals(3, secondYear.partsList!!.size)
    }

    @Test
    fun `Title object should extract child manifestations`() {
        val manifestations = mutableListOf<CollectionsPartsObject>()
        singleTitle.partsList!!.forEach { yearWorks -> yearWorks.getPartRefs().forEach { manifestations.add(it) } }
        Assertions.assertEquals(4, manifestations.size)

        val manifest1 = manifestations.first().partsReference!!
        Assertions.assertEquals("18", manifest1.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", manifest1.getName())
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION, manifest1.getRecordType())
        Assertions.assertEquals(1, manifest1.partsList!!.size)

        val manifest2 = manifestations[1].partsReference!!
        Assertions.assertEquals("22", manifest2.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.10", manifest2.getName())
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION, manifest2.getRecordType())
        Assertions.assertEquals(null, manifest2.partsList)

        val manifest3 = manifestations[2].partsReference!!
        Assertions.assertEquals("20", manifest3.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", manifest3.getName())
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION, manifest3.getRecordType())
        Assertions.assertEquals(1, manifest3.partsList!!.size)

        val manifest4 = manifestations[3].partsReference!!
        Assertions.assertEquals("10", manifest4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", manifest4.getName())
        Assertions.assertEquals(AxiellRecordType.MANIFESTATION, manifest4.getRecordType())
        Assertions.assertEquals(2, manifest4.partsList!!.size)
    }

    @Test
    fun `Title object should extract child items`() {
        val items = mutableListOf<CollectionsPartsObject>()
        singleTitle.partsList!!.forEach { yearWorks ->
            yearWorks.getPartRefs().forEach { manifestations ->
                manifestations.getPartRefs().forEach { items.add(it) }
            }
        }
        Assertions.assertEquals(4, items.size)

        val item1 = items.first().partsReference!!
        Assertions.assertEquals("19", item1.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", item1.getName())
        Assertions.assertEquals(AxiellRecordType.ITEM, item1.getRecordType())
        Assertions.assertEquals(AxiellFormat.PHYSICAL, item1.getFormat())

        val item2 = items[1].partsReference!!
        Assertions.assertEquals("21", item2.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", item2.getName())
        Assertions.assertEquals(AxiellRecordType.ITEM, item2.getRecordType())
        Assertions.assertEquals(AxiellFormat.DIGITAL, item2.getFormat())

        val item3 = items[2].partsReference!!
        Assertions.assertEquals("6", item3.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", item3.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM, item3.getRecordType())
        Assertions.assertEquals(AxiellFormat.PHYSICAL, item3.getFormat())

        val item4 = items[3].partsReference!!
        Assertions.assertEquals("5", item4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", item4.titleList!!.first().title)
        Assertions.assertEquals(AxiellRecordType.ITEM, item4.getRecordType())
        Assertions.assertEquals(AxiellFormat.DIGITAL, item4.getFormat())
    }

}
