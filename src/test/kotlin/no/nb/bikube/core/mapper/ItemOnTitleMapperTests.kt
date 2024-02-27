package no.nb.bikube.core.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalog.collections.mapper.mapCollectionsPartsObjectToGenericItem
import no.nb.bikube.catalog.collections.model.*
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
class ItemOnTitleMapperTests {

    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleItemJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleSingle.json")
    private val singleItem = mapper().readValue<CollectionsModel>(singleItemJson).getFirstObject()!!

    private fun itemList(): List<Item> {
        val list = mutableListOf<Item>()
        singleItem.partsList!!.forEach { yearWork ->
            yearWork.getPartRefs().forEach { manifest ->
                manifest.getPartRefs().forEach { item ->
                    list.add(mapCollectionsPartsObjectToGenericItem(
                        item.partsReference!!,
                        singleItem.priRef,
                        singleItem.getName(),
                        singleItem.getMaterialType()?.norwegian
                    ))
                }
            }
        }
        return list
    }
    private val mappedList = itemList()

    @Test
    fun `Item-on-title mapper should have all item`() {
        Assertions.assertEquals(4, mappedList.size)
    }

    @Test
    fun `Item-on-title mapper should map catalogueId`() {
        Assertions.assertEquals("19", mappedList[0].catalogueId)
        Assertions.assertEquals("21", mappedList[1].catalogueId)
        Assertions.assertEquals("6", mappedList[2].catalogueId)
        Assertions.assertEquals("5", mappedList[3].catalogueId)
    }

    @Test
    fun `Item-on-title mapper should map names`() {
        Assertions.assertEquals("Bikubeavisen 2011.01.24", mappedList[0].name)
        Assertions.assertEquals("Bikubeavisen 2012.01.09", mappedList[1].name)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", mappedList[2].name)
        Assertions.assertEquals("Bikubeavisen 2012.01.02", mappedList[3].name)
    }

    @Test
    fun `Item-on-title mapper should map dates`() {
        Assertions.assertEquals(LocalDate.parse("2011-01-24"), mappedList[0].date)
        Assertions.assertEquals(LocalDate.parse("2012-01-09"), mappedList[1].date)
        Assertions.assertEquals(LocalDate.parse("2012-01-02"), mappedList[2].date)
        Assertions.assertEquals(LocalDate.parse("2012-01-02"), mappedList[3].date)
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
            it.titleCatalogueId == "3"
        })
    }

    @Test
    fun `Item-on-title mapper should map title name`() {
        Assertions.assertTrue(mappedList.all {
            it.titleName == "Bikubeavisen"
        })
    }

    @Test
    fun `Item-on-title mapper should map if digital or not`() {
        Assertions.assertEquals(false, mappedList[0].digital)
        Assertions.assertEquals(true, mappedList[1].digital)
        Assertions.assertEquals(false, mappedList[2].digital)
        Assertions.assertEquals(true, mappedList[3].digital)
    }
}
