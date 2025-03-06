package no.nb.bikube.catalogue.collections.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.enum.MaterialType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate

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
        Assertions.assertEquals(3, items.size)
    }

    @Test
    fun `Collection object should extract priRefs `() {
        Assertions.assertEquals("1601048432", items.first().priRef)
        Assertions.assertEquals("1601048433", items[1].priRef)
        Assertions.assertEquals("1601048434", items[2].priRef)
    }

    @Test
    fun `Collection object should extract submediums`() {
        Assertions.assertTrue(items.all { it.getMaterialType() == MaterialType.NEWSPAPER })
    }

    @Test
    fun `Collection object should extract record types`() {
        Assertions.assertTrue(items.all { it.getRecordType() == CollectionsRecordType.ITEM })
    }

    @Test
    fun `Collection object should extract formats`() {
        Assertions.assertEquals(CollectionsFormat.PHYSICAL, items.first().getFormat())
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items[1].getFormat())
        Assertions.assertEquals(CollectionsFormat.DIGITAL, items[2].getFormat())
    }

    @Test
    fun `Collection object should extract parent manifestations`() {
        val mani1 = items.first().getFirstPartOf()!!
        Assertions.assertEquals("1601048429", mani1.priRef)
        Assertions.assertEquals(LocalDate.parse("2024-01-01"), mani1.getDate())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani1.getMaterialType())

        // Manifestation 1 and 2 should be the same
        val mani2 = items[1].getFirstPartOf()!!
        Assertions.assertEquals(mani1, mani2)

        val mani3 = items[2].getFirstPartOf()!!
        Assertions.assertEquals("1601048430", mani3.priRef)
        Assertions.assertEquals(LocalDate.parse("2024-01-02"), mani3.getDate())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, mani3.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, mani3.getMaterialType())
    }

    @Test
    fun `Collection object should extract parent serial works`() {
        // All items are connected to the same title
        val title1 = items.first().getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("1601048426", title1.priRef)
        Assertions.assertEquals("Bikubetestavisen", title1.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, title1.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, title1.getMaterialType())

        val title2 = items[1].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title2)

        val title3 = items[2].getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals(title1, title3)
    }
}
