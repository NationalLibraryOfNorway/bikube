package no.nb.bikube.core.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.model.CollectionsModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class ItemMapperTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperItemSingle.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).adlibJson.recordList!!.first()
    private val genericItem = mapCollectionsObjectToGenericItem(singleItem)

    @Test
    fun `Item mapper should map catalogueId`() { Assertions.assertEquals("5", genericItem.catalogueId) }

    @Test
    fun `Item mapper should map name`() { Assertions.assertEquals("Bikubeavisen 2012.01.02", genericItem.name) }

    @Test
    fun `Item mapper should map date`() { Assertions.assertEquals(LocalDate.parse("2012-01-02"), genericItem.date) }

    @Test
    fun `Item mapper should map material type`() { Assertions.assertEquals("Avis", genericItem.materialType) }

    @Test
    fun `Item mapper should map title ID`() { Assertions.assertEquals("3", genericItem.titleCatalogueId) }

    @Test
    fun `Item mapper should map title name`() { Assertions.assertEquals("Bikubeavisen", genericItem.titleName) }

    @Test
    fun `Item mapper should map if digital`() { Assertions.assertEquals(true, genericItem.digital) }

}
