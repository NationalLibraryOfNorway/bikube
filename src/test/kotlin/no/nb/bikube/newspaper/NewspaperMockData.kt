package no.nb.bikube.newspaper

import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Language
import no.nb.bikube.core.model.Title
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

        // Equal to collectionsModelMockTitleE, valid for insert without catalogueId
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

        val newspaperTitleMockC = Title(
            name = "Avis C",
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = null,
            language = null,
            materialType = MaterialType.NEWSPAPER.norwegian,
            catalogueId = "3"
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

       // Equals to collectionsModelMockItemB - used for creation
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

        // Minimum valid for creating digital item
        val newspaperItemMockCValidForCreation = Item(
            catalogueId = null,
            name = null,
            date = LocalDate.parse("2020-01-01"),
            materialType = null,
            titleCatalogueId = "1",
            titleName = null,
            digital = true,
            urn = "avisa_null_null_20200101_1_1_1"
        )

        // Minimum valid for creating title
        val newspaperTitleMockDValidForCreation = Title(
            catalogueId = null,
            name = "Avis D",
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = null,
            language = null,
            materialType = null
        )

        val language: Language = Language(
            name = "nob",
            databaseId = "1"
        )
    }
}
