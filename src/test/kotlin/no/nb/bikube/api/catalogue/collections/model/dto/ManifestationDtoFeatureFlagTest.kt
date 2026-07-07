package no.nb.bikube.api.catalogue.collections.model.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.api.catalogue.collections.config.CollectionsLrefConfig
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ManifestationDtoFeatureFlagTest {

    private val lrefConfig = CollectionsLrefConfig(
        mavisId = "1",
        argang = "2",
        avisnr = "3",
        versjon = "4",
        originalTittel = "5",
        alternativTittel = "6",
        mediumTekst = "7",
        submediumAviser = "8",
        itemStatusPliktavlevert = "9"
    )

    private val parentId = "42"

    private fun buildDto(useSeriesManifestation: Boolean) = createManifestationDto(
        lrefConfig = lrefConfig,
        id = "100",
        objectNumber = "NP-100",
        parentCatalogueId = parentId,
        database = CollectionsDatabase.NEWSPAPER,
        date = LocalDate.of(2020, 1, 1),
        username = "test-user",
        useSeriesManifestation = useSeriesManifestation
    )

    @Test
    fun `flag off - dto should link to parent via part_of_reference_lref`() {
        val dto = buildDto(useSeriesManifestation = false)
        Assertions.assertEquals(parentId, dto.partOfReference)
        Assertions.assertNull(dto.seriesTitleLref)
    }

    @Test
    fun `flag on - dto should link to series via series_title_lref`() {
        val dto = buildDto(useSeriesManifestation = true)
        Assertions.assertNull(dto.partOfReference)
        Assertions.assertEquals(parentId, dto.seriesTitleLref)
    }

    @Test
    fun `flag off - JSON should contain part_of_reference_lref and not series_title_lref`() {
        val json = Json.encodeToString(buildDto(useSeriesManifestation = false))
        Assertions.assertTrue(json.contains("\"part_of_reference.lref\":\"$parentId\""))
        Assertions.assertFalse(json.contains("\"series.title.lref\":\"$parentId\""))
    }

    @Test
    fun `flag on - JSON should contain series_title_lref and not part_of_reference_lref`() {
        val json = Json.encodeToString(buildDto(useSeriesManifestation = true))
        Assertions.assertTrue(json.contains("\"series.title.lref\":\"$parentId\""))
        Assertions.assertFalse(json.contains("\"part_of_reference.lref\":\"$parentId\""))
    }
}
