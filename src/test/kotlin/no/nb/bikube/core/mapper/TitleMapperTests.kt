package no.nb.bikube.core.mapper

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.collections.CollectionsDating
import no.nb.bikube.core.model.collections.CollectionsModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class TitleMapperTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleTitleJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleSingle.json")
    private val singleTitle = mapper().readValue<CollectionsModel>(singleTitleJson).adlibJson.recordList!!.first()
    private val genericTitle = mapCollectionsObjectToGenericTitle(singleTitle)

    @Test
    fun `Title mapper should map catalogueId `() {
        Assertions.assertEquals("3", genericTitle.catalogueId)
    }

    @Test
    fun `Title mapper should map start date`() {
        Assertions.assertEquals(LocalDate.parse("1947-01-01"), genericTitle.startDate)
    }

    @Test
    fun `Title mapper should map end date`() {
        Assertions.assertEquals(LocalDate.parse("1999-01-09"), genericTitle.endDate)
    }

    @Test
    fun `Title mapper should map language`() {
        Assertions.assertEquals("nob", genericTitle.language)
    }

    @Test
    fun `Title mapper should map material type`() {
        Assertions.assertEquals(MaterialType.NEWSPAPER.norwegian, genericTitle.materialType)
    }

    @Test
    fun `Title mapper should map title`() {
        Assertions.assertEquals("Bikubeavisen", genericTitle.name)
    }

    @Test
    fun `Title mapper should map publication place `() {
        Assertions.assertEquals("Mo i Rana", genericTitle.publisherPlace)
    }

    @Test
    fun `Title mapper should map publisher`() {
        Assertions.assertEquals("Amedia", genericTitle.publisher)
    }

    @Test
    fun `Title mapper should map year dates`() {
        val testTitle = singleTitle.copy(datingList = listOf(CollectionsDating("1979", "1999")))
        val testGenericTitle = mapCollectionsObjectToGenericTitle(testTitle)
        Assertions.assertEquals(LocalDate.parse("1979-01-01"), testGenericTitle.startDate)
        Assertions.assertEquals(LocalDate.parse("1999-01-01"), testGenericTitle.endDate)
    }

    @Test
    fun `Title mapper should map date dates`() {
        val testTitle = singleTitle.copy(datingList = listOf(CollectionsDating("1979-01-01", "1999-08-09")))
        val testGenericTitle = mapCollectionsObjectToGenericTitle(testTitle)
        Assertions.assertEquals(LocalDate.parse("1979-01-01"), testGenericTitle.startDate)
        Assertions.assertEquals(LocalDate.parse("1999-08-09"), testGenericTitle.endDate)
    }
}
