package no.nb.bikube.api.catalogue.collections.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.core.enum.MaterialType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.LocalDate
import kotlin.collections.get

@SpringBootTest
@ActiveProfiles("test")
class CollectionsModelFromJsonSingleNewspaperTitleTests {
    private fun mapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        // Disable unknown properties as we are not using all fields, and will test for what we need
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    private val singleTitleJson = File("src/test/resources/CollectionsJsonTestFiles/NewspaperTitleSingle.json")
    private val singleTitle = mapper().readValue<CollectionsModel>(singleTitleJson).getFirstObject()

    @Test
    fun `Title object should extract priRef`() { Assertions.assertEquals("1601048426", singleTitle.priRef) }

    @Test
    fun `Title object should extract start date`() { Assertions.assertEquals(LocalDate.parse("2024-01-01"), singleTitle.getStartDate()) }

    @Test
    fun `Title object should extract end date`() { Assertions.assertEquals(LocalDate.parse("2024-03-31"), singleTitle.getEndDate()) }

    @Test
    fun `Title object should extract language`() { Assertions.assertEquals("Norsk bokmål", singleTitle.getLanguage()) }

    @Test
    fun `Title object should extract submedium`() {
        Assertions.assertEquals(MaterialType.NEWSPAPER, singleTitle.getMaterialType())
    }

    @Test
    fun `Title object should extract title`() { Assertions.assertEquals("Bikubetestavisen", singleTitle.getName()) }

    @Test
    fun `Title object should extract publication place `() { Assertions.assertEquals("Norge;Nordland;;Rana;;;;", singleTitle.getPublisherPlace()) }

    @Test
    fun `Title object should extract publisher`() { Assertions.assertEquals("Nasjonalbiblioteket", singleTitle.getPublisher()) }

    @Test
    fun `Title object should extract child manifestations`() {
        val manifestations = singleTitle.partsList!!
        Assertions.assertEquals(3, manifestations.size)

        val manifest1 = manifestations.first().partsReference!!
        Assertions.assertEquals("1601048429", manifest1.priRef)
        Assertions.assertEquals(LocalDate.parse("2024-01-01"), manifest1.getDate())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, manifest1.getRecordType())
        Assertions.assertEquals("2024-01-01", manifest1.date!!.first())

        val manifest2 = manifestations[1].partsReference!!
        Assertions.assertEquals("1601048430", manifest2.priRef)
        Assertions.assertEquals(LocalDate.parse("2024-01-02"), manifest2.getDate())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, manifest2.getRecordType())
        Assertions.assertEquals("2024-01-02", manifest2.date!!.first())

        val manifest3 = manifestations[2].partsReference!!
        Assertions.assertEquals("1601048431", manifest3.priRef)
        Assertions.assertEquals(LocalDate.parse("2024-01-03"), manifest3.getDate())
        Assertions.assertEquals(CollectionsRecordType.MANIFESTATION, manifest3.getRecordType())
        Assertions.assertEquals("2024-01-03", manifest3.date!!.first())
    }
}
