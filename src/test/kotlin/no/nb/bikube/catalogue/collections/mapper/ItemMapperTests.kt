package no.nb.bikube.catalogue.collections.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartsObjectMockItemA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartsObjectMockItemC
import no.nb.bikube.catalogue.collections.model.CollectionsModel
import no.nb.bikube.catalogue.collections.model.getFirstObject
import no.nb.bikube.core.enum.MaterialType
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

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperItemSingleDigital.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).getFirstObject()!!
    private val genericItem = mapCollectionsObjectToGenericItem(singleItem)
    private val collectionsPartsObjWithDateInTitle = collectionsPartsObjectMockItemA.partsReference!!
    private val collectionsPartsObjWithoutDateInTitle = collectionsPartsObjectMockItemC.partsReference!!
    @Test
    fun `Item mapper should map catalogueId`() { Assertions.assertEquals("39979", genericItem.catalogueId) }

    @Test
    fun `Item mapper should map name`() { Assertions.assertEquals("Bikubetestavisen", genericItem.name) }

    @Test
    fun `Item mapper should map date`() { Assertions.assertEquals(LocalDate.parse("2024-01-01"), genericItem.date) }

    @Test
    fun `Item mapper should map material type`() { Assertions.assertEquals(MaterialType.NEWSPAPER.norwegian, genericItem.materialType) }

    @Test
    fun `Item mapper should map title ID`() { Assertions.assertEquals("39977", genericItem.titleCatalogueId) }

    @Test
    fun `Item mapper should map title name`() { Assertions.assertEquals("Bikubetestavisen", genericItem.titleName) }

    @Test
    fun `Item mapper should map if digital`() { Assertions.assertEquals(true, genericItem.digital) }

    @Test
    fun `Item mapper should use date field if provided`() {
        val partsObjItem = mapCollectionsPartsObjectToGenericItem(
            collectionsPartsObjWithDateInTitle,
            "1",
            "Bikubeavisen",
            MaterialType.NEWSPAPER.norwegian,
            "1337-01-01"
        )
        Assertions.assertEquals(LocalDate.parse("1337-01-01"), partsObjItem.date)
    }

    @Test
    // TODO: Check if this should be deleted or it if works
    fun `Item mapper should use date from title if date field is not provided`() {
        val collectionsPartsObjWithoutDateInTitle = collectionsPartsObjectMockItemA.partsReference!!
        val partsObjItemWithoutDate = mapCollectionsPartsObjectToGenericItem(
            collectionsPartsObjWithoutDateInTitle,
            "1",
            "Bikubeavisen",
            MaterialType.NEWSPAPER.norwegian
        )
        Assertions.assertEquals(LocalDate.parse("2020-01-01"), partsObjItemWithoutDate.date)
    }

    @Test
    fun `Item mapper should set date to null if date field is not provided and title does not contain date`() {
        val partsObjItemWithoutDate = mapCollectionsPartsObjectToGenericItem(
            collectionsPartsObjWithoutDateInTitle,
            "1",
            "Bikubeavisen",
            MaterialType.NEWSPAPER.norwegian
        )
        Assertions.assertEquals(null, partsObjItemWithoutDate.date)
    }
}
