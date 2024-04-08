package no.nb.bikube.catalogue.collections.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalogue.collections.enum.CollectionsDescriptionType
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
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
        Assertions.assertEquals(5, items.size)
    }

    @Test
    fun `Collection object should extract priRefs `() {
        Assertions.assertEquals("39979", items.first().priRef)
        Assertions.assertEquals("39981", items[1].priRef)
        Assertions.assertEquals("39983", items[2].priRef)
        Assertions.assertEquals("39984", items[3].priRef)
        Assertions.assertEquals("39985", items[4].priRef)
    }

    @Test
    fun `Collection object should extract submediums`() {
        Assertions.assertTrue(items.all { it.getMaterialType() == MaterialType.NEWSPAPER })
    }

    @Test
    fun `Collection object should extract names`() {
        Assertions.assertTrue(items.all { it.getName() == "Bikubetestavisen" })
    }

    @Test
    fun `Collection object should extract record types`() {
        Assertions.assertTrue(items.all { it.getRecordType() == CollectionsRecordType.ITEM })
    }

    @Test
    fun `Collection object should extract formats`() {
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items.first().getFormat())
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items[1].getFormat())
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items[2].getFormat())
        Assertions.assertEquals(CollectionsFormat.PHYSICAL, items[3].getFormat())
        Assertions.assertEquals(CollectionsFormat.PHYSICAL, items[4].getFormat())
    }

    @Test
    fun `Collection object should extract parent manifestations`() {
        val mani1 = items.first().getFirstPartOf()!!
        Assertions.assertEquals("39978", mani1.priRef)
        Assertions.assertEquals("Bikubetestavisen", mani1.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani1.getMaterialType())

        // Manifestation 1 and 2 should be the same
        val mani2 = items[1].getFirstPartOf()!!
        Assertions.assertEquals("39980", mani2.priRef)
        Assertions.assertEquals("Bikubetestavisen", mani2.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani2.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani2.getMaterialType())

        val mani3 = items[2].getFirstPartOf()!!
        Assertions.assertEquals("39982", mani3.priRef)
        Assertions.assertEquals("Bikubetestavisen", mani3.getName())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani3.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani3.getMaterialType())

        val mani4 = items[3].getFirstPartOf()!!
        Assertions.assertEquals(mani1, mani4)

        val mani5 = items[4].getFirstPartOf()!!
        Assertions.assertEquals(mani2, mani5)
    }

    @Test
    fun `Collection object should extract parent serial works`() {
        // All items are connected to the same title
        val title1 = items.first().getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("39977", title1.priRef)
        Assertions.assertEquals("Bikubetestavisen", title1.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, title1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, title1.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.SERIAL, title1.getWorkType())

        val title2 = items[1].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title2)

        val title3 = items[2].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title3)

        val title4 = items[3].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title4)

        val title5 = items[4].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title5)
    }
}
