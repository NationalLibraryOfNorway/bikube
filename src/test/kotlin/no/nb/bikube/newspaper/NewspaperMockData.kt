package no.nb.bikube.newspaper

import no.nb.bikube.core.model.*
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

        // Equal to collectionsModelMockTitleE
        val newspaperTitleMockB = Title(
            name = "Avis B",
            startDate = LocalDate.parse("2020-01-01"),
            endDate = LocalDate.parse("2020-01-31"),
            publisher = "B-Forlaget",
            publisherPlace = "Brakka",
            language = null,
            materialType = "Avis",
            catalogueId = "2"
        )

        val newspaperTitleMockC = Title(
            name = "Avis C",
            startDate = null,
            endDate = null,
            publisher = null,
            publisherPlace = null,
            language = null,
            materialType = "Avis",
            catalogueId = "3"
        )

        val newspaperItemMockA = Item(
            catalogueId = "2",
            name = "Avis A 2020.01.01",
            date = null,
            materialType = "Avis",
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            titleName = newspaperTitleMockA.name,
            digital = true,
            urn = "avisa_null_null_20200101_1_1_1"
        )

        val language: Language = Language(
            name = "nob",
            databaseId = "1"
        )
    }
}
