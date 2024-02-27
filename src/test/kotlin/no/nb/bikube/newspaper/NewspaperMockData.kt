package no.nb.bikube.newspaper

import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.TitleInputDto
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
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = null,
            language = null
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
            urn = "avisa_null_null_20200101_1_1_1"
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
            urn = "avisa_null_null_20200105_1_1_1"
        )

        // Equals to collectionsModelMockItemB
        val newspaperInputDtoItemMockB = ItemInputDto(
            date = LocalDate.parse("2020-01-05"),
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            digital = true,
            urn = "avisa_null_null_20200105_1_1_1",
            title = "Avis A 2020.01.05"
        )

        // Minimum valid for creating digital item
        val newspaperItemMockCValidForCreation = ItemInputDto(
            date = LocalDate.parse("2020-01-01"),
            titleCatalogueId = "1",
            digital = true,
            urn = "avisa_null_null_20200101_1_1_1",
            title = "Avis A 2020.01.01"
        )

        val language: Language = Language(
            name = "nob",
            catalogueId = "1"
        )
    }
}
