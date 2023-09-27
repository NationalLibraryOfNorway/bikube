package no.nb.bikube.newspaper

import no.nb.bikube.core.model.*

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

        val newspaperItemMockA = Item(
            catalogueId = "2",
            name = "Avis A 2020.01.01",
            date = null,
            materialType = "Avis",
            titleCatalogueId = newspaperTitleMockA.catalogueId,
            titleName = newspaperTitleMockA.name,
            digital = true
        )

        val collectionsModelMockA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(CollectionsObject(
                    priRef = "1",
                    titleList = null,
                    recordTypeList = null,
                    formatList = null,
                    partOfList = null,
                    subMediumList = null,
                    mediumList = null,
                    datingList = null,
                    publisherList = null,
                    languageList = null,
                    placeOfPublicationList = null,
                    partsList = null,
                    workTypeList = null
                ))
            )
        )
    }
}
