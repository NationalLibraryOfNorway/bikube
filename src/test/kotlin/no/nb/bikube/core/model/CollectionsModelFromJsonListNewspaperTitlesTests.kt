package no.nb.bikube.core.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.enum.CollectionsRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.collections.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File

@SpringBootTest
@ActiveProfiles("test")
class CollectionsModelFromJsonListNewspaperTitlesTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val titleListJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleList.json")
    private val titles = mapper().readValue<CollectionsModel>(titleListJson).getObjects()!!

    @Test
    fun `Title object should extract all items`() {
        Assertions.assertEquals(3, titles.size)
    }

    @Test
    fun `Title object should extract priRefs`() {
        Assertions.assertEquals("3", titles.first().priRef)
        Assertions.assertEquals("7", titles[1].priRef)
        Assertions.assertEquals("9", titles[2].priRef)
    }

    @Test
    fun `Title object should extract submediums`() {
        Assertions.assertTrue(titles.all { it.getMaterialType() == MaterialType.NEWSPAPER })
    }

    @Test
    fun `Title object should extract title`() {
        Assertions.assertEquals("Bikubeavisen", titles.first().getName())
        Assertions.assertEquals("Bikubeposten", titles[1].getName())
        Assertions.assertEquals("The Bikube", titles[2].getName())
    }

    @Test
    fun `Title object should extract record types`() {
        Assertions.assertTrue(titles.all { it.getRecordType() == CollectionsRecordType.WORK })
    }

}
