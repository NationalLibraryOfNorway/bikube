package no.nb.bikube.catalogue.collections.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.Item
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class ManifestationOnTitleMapperTests {

    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleSingle.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).getFirstObject()

    private fun manifestationList(): List<Item> {
        val list = mutableListOf<Item>()
        singleItem.partsList!!.forEach { manifestation ->
            list.add(mapCollectionsPartsObjectToGenericItem(
                manifestation.partsReference!!,
                singleItem.priRef,
                singleItem.getName(),
                singleItem.getMaterialType()?.norwegian,
                manifestation.getStartDate().toString()
            ))
        }
        return list
    }
    private val mappedList = manifestationList()

    @Test
    fun `Item-on-title mapper should have all items`() {
        Assertions.assertEquals(3, mappedList.size)
    }

    @Test
    fun `Item-on-title mapper should map catalogueId`() {
        Assertions.assertEquals("1601048429", mappedList[0].catalogueId)
        Assertions.assertEquals("1601048430", mappedList[1].catalogueId)
        Assertions.assertEquals("1601048431", mappedList[2].catalogueId)
    }

    @Test
    fun `Item-on-title mapper should map dates`() {
        Assertions.assertEquals(LocalDate.parse("2024-01-01"), mappedList[0].date)
        Assertions.assertEquals(LocalDate.parse("2024-01-02"), mappedList[1].date)
        Assertions.assertEquals(LocalDate.parse("2024-01-03"), mappedList[2].date)
    }

    @Test
    fun `Item-on-title mapper should map material types`() {
        Assertions.assertTrue(mappedList.all {
            it.materialType == MaterialType.NEWSPAPER.norwegian
        })
    }

    @Test
    fun `Item-on-title mapper should map title ID`() {
        Assertions.assertTrue(mappedList.all {
            it.titleCatalogueId == "1601048426"
        })
    }

    @Test
    fun `Item-on-title mapper should map title name`() {
        Assertions.assertTrue(mappedList.all {
            it.titleName == "Bikubetestavisen"
        })
    }
}
