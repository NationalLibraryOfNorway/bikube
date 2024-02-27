package no.nb.bikube.core.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalog.collections.enum.CollectionsDescriptionType
import no.nb.bikube.catalog.collections.enum.CollectionsFormat
import no.nb.bikube.catalog.collections.enum.CollectionsRecordType
import no.nb.bikube.catalog.collections.model.*
import no.nb.bikube.core.enum.MaterialType
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
    private val items = mapper().readValue<CollectionsModel>(itemListJson).getObjects()!!

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
        Assertions.assertTrue(items.all { it.getMaterialType() == MaterialType.NEWSPAPER })
    }

    @Test
    fun `Collection object should extract names`() {
        Assertions.assertEquals("Bikubeavisen 2012.01.02", items.first().getName())
        Assertions.assertEquals("Bikubeavisen 2012.01.02", items[1].getName())
        Assertions.assertEquals("Bikubeavisen 2011.01.24", items[2].getName())
        Assertions.assertEquals("Bikubeavisen 2012.01.09", items[3].getName())
    }

    @Test
    fun `Collection object should extract record types`() {
        Assertions.assertTrue(items.all { it.getRecordType() == CollectionsRecordType.ITEM })
    }

    @Test
    fun `Collection object should extract formats`() {
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items.first().getFormat())
        Assertions.assertEquals(CollectionsFormat.PHYSICAL, items[1].getFormat())
        Assertions.assertEquals(CollectionsFormat.PHYSICAL, items[2].getFormat())
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items[3].getFormat())
    }

    @Test
    fun `Collection object should extract parent manifestations`() {
        val mani1 = items.first().getFirstPartOf()!!
        Assertions.assertEquals("10", mani1.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", mani1.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani1.getMaterialType())

        // Manifestation 1 and 2 should be the same
        val mani2 = items[1].getFirstPartOf()!!
        Assertions.assertEquals(mani1, mani2)

        val mani3 = items[2].getFirstPartOf()!!
        Assertions.assertEquals("18", mani3.priRef)
        Assertions.assertEquals("Bikubeavisen 2011.01.24", mani3.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani3.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani3.getMaterialType())

        val mani4 = items[3].getFirstPartOf()!!
        Assertions.assertEquals("20", mani4.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", mani4.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani4.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani4.getMaterialType())
    }

    @Test
    fun `Collection object should extract parent year works`() {
        val yearWork1 = items.first().getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("4", yearWork1.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", yearWork1.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, yearWork1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, yearWork1.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.YEAR, yearWork1.getWorkType())

        // Year work 1, 2 and 4 should be the same
        val yearWork2 = items[1].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(yearWork1, yearWork2)

        val yearWork3 = items[2].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("11", yearWork3.priRef)
        Assertions.assertEquals("Bikubeavisen 2011", yearWork3.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, yearWork3.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, yearWork3.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.YEAR, yearWork3.getWorkType())

        val yearWork4 = items[3].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(yearWork1, yearWork4)
    }

    @Test
    fun `Collection object should extract parent serial works`() {
        // All items are connected to the same title
        val title1 = items.first().getFirstPartOf()!!.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("3", title1.priRef)
        Assertions.assertEquals("Bikubeavisen", title1.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, title1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, title1.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.SERIAL, title1.getWorkType())

        val title2 = items[1].getFirstPartOf()!!.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title2)

        val title3 = items[2].getFirstPartOf()!!.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title3)

        val title4 = items[3].getFirstPartOf()!!.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title4)
    }
}
