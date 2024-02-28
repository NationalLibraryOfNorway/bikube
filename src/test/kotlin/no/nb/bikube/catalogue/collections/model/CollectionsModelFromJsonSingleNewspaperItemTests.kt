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
class CollectionsModelFromJsonSingleNewspaperItemTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperItemSingle.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).getFirstObject()!!

    @Test
    fun `Item object should extract priRef`() { Assertions.assertEquals("5", singleItem.priRef) }

    @Test
    fun `Item object should extract format`() {
        Assertions.assertEquals(CollectionsFormat.DIGITAL, singleItem.getFormat())
    }

    @Test
    fun `Item object should extract medium`() {
        Assertions.assertEquals(
            "Tekst",
            singleItem.mediumList!!.first().medium
        )
    }

    @Test
    fun `Item object should extract submedium`() {
        Assertions.assertEquals(MaterialType.NEWSPAPER, singleItem.getMaterialType())
    }

    @Test
    fun `Item object should extract title`() {
        Assertions.assertEquals("Bikubeavisen 2012.01.02", singleItem.getName())
    }

    @Test
    fun `Item object should extract recordtype`() {
        Assertions.assertEquals(CollectionsRecordType.ITEM, singleItem.getRecordType())
    }

    @Test
    fun `Item object should extract parent manifestation`() {
        val manifestation = singleItem.getFirstPartOf()!!
        Assertions.assertEquals("10", manifestation.priRef)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", manifestation.getName())
        Assertions.assertEquals(MaterialType.NEWSPAPER, manifestation.getMaterialType())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, manifestation.getRecordType())
    }

    @Test
    fun `Item object should extract parent year work`() {
        val yearWork = singleItem.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("4", yearWork.priRef)
        Assertions.assertEquals("Bikubeavisen 2012", yearWork.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, yearWork.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, yearWork.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.YEAR, yearWork.getWorkType())
    }

    @Test
    fun `Item object should extract parent title`() {
        val title = singleItem.getFirstPartOf()!!.getFirstPartOf()!!.getFirstPartOf()!!
        Assertions.assertEquals("3", title.priRef)
        Assertions.assertEquals("Bikubeavisen", title.getName())
        Assertions.assertEquals(CollectionsRecordType.WORK, title.getRecordType())
        Assertions.assertEquals(MaterialType.NEWSPAPER, title.getMaterialType())
        Assertions.assertEquals(CollectionsDescriptionType.SERIAL, title.getWorkType())
    }

}
