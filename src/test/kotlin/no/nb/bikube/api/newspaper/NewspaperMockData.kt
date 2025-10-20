package no.nb.bikube.api.newspaper

import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import no.nb.bikube.api.catalogue.collections.model.dto.AlternativeNumberInput
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.model.Language
import no.nb.bikube.api.core.model.Title
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.core.model.inputDto.ItemUpdateDto
import no.nb.bikube.api.core.model.inputDto.MissingPeriodicalItemDto
import no.nb.bikube.api.core.model.inputDto.TitleInputDto
import java.time.LocalDate

class NewspaperMockData {
    companion object {
        val newspaperTitleMockA = Title(
            name = "Avis A",
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = "Mo I Rana",
            language = null,
            materialType = null,
            catalogueId = "1"
        )

        // Minimum valid for creation
        val newspaperTitleInputDtoMockA = TitleInputDto(
            name = "Avis A",
            username = CollectionsModelMockData.Companion.TEST_USERNAME,
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = null,
            language = "nob"
        )

        // Equal to collectionsModelMockTitleE, insert DTO below
        val newspaperTitleMockB = Title(
            name = "Avis B",
            startDate = LocalDate.parse("2020-01-01"),
            endDate = LocalDate.parse("2020-01-31"),
            publisher = "B-Forlaget",
            publisherPlace = "Brakka",
            language = "nob",
            materialType = MaterialType.NEWSPAPER.norwegian,
            catalogueId = "2"
        )

        // Equal to collectionsModelMockTitleE, valid for insert
        val newspaperTitleInputDtoMockB = TitleInputDto(
            name = "Avis B",
            username = CollectionsModelMockData.Companion.TEST_USERNAME,
            startDate = LocalDate.parse("2020-01-01"),
            endDate = LocalDate.parse("2020-01-31"),
            publisher = "B-Forlaget",
            publisherPlace = "Brakka",
            language = "nob"
        )

        val newspaperItemMockA = Item(
            catalogueId = "2",
            name = "Avis A 2020.01.01",
            date = null,
            materialType = MaterialType.NEWSPAPER.norwegian,
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            titleName = newspaperTitleMockA.name,
            digital = true,
            urn = "avisa_null_null_20200101_1_1_1",
            parentCatalogueId = null
        )

       // Equals to collectionsModelMockItemB
        val newspaperItemMockB = Item(
            catalogueId = "46",
            name = "Avis A 2020.01.05",
            date = LocalDate.parse("2020-01-05"),
            materialType = null,
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            titleName = null,
            digital = true,
            urn = "avisa_null_null_20200105_1_1_1",
            parentCatalogueId = null
        )

        // Equals to collectionsModelMockItemB
        val newspaperInputDtoItemMockB = ItemInputDto(
            date = LocalDate.parse("2020-01-05"),
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            username = CollectionsModelMockData.Companion.TEST_USERNAME,
            digital = true,
            urn = "avisa_null_null_20200105_1_1_1",
            containerId = null,
            number = "12",
            volume = "13",
            version = "14"
        )

        // Minimum valid for creating digital item
        val newspaperItemMockCValidForCreation = ItemInputDto(
            date = LocalDate.parse("2020-01-01"),
            titleCatalogueId = "1",
            username = CollectionsModelMockData.Companion.TEST_USERNAME,
            digital = true,
            urn = "avisa_null_null_20200101_1_1_1",
            itemStatus = "Digitalisert",
            containerId = null,
            number = "15",
            volume = "16",
            version = "17"
        )

        val language: Language = Language(
            name = "nob",
            catalogueId = "1"
        )

        val urnMock = AlternativeNumberInput(
            name = newspaperItemMockB.urn!!,
            type = "URN"
        )

        val newspaperAlternativeNumbers = listOf(
            AlternativeNumberInput("1", "Årgang"),
            AlternativeNumberInput("1", "Avisnr"),
            AlternativeNumberInput("1", "Versjon"),
        )

        val missingItemDtoMock = MissingPeriodicalItemDto(
            date = LocalDate.parse("2020-01-01"),
            titleCatalogueId = "1",
            username = CollectionsModelMockData.Companion.TEST_USERNAME
        )

        // Equals to collectionsModelMockManifestationB
        val newspaperItemMockDNoItem = Item(
            catalogueId = "46",
            name = "Bikubeavisen",
            date = missingItemDtoMock.date,
            materialType = MaterialType.NEWSPAPER.norwegian,
            titleCatalogueId = missingItemDtoMock.titleCatalogueId,
            titleName = "Bikubeavisen",
            digital = null,
            urn = null,
            parentCatalogueId = "22"
        )

        // ID equals collectionsModelMockManifestationB
        val newspaperItemUpdateDtoMockA = ItemUpdateDto(
            manifestationId = "24",
            username = CollectionsModelMockData.Companion.TEST_USERNAME,
            notes = CollectionsModelMockData.Companion.TEST_NOTES,
            number = "1"
        )
    }
}
