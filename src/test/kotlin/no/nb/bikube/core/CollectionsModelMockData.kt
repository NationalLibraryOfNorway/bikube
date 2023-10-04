package no.nb.bikube.core

import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.*

class CollectionsModelMockData {
    companion object {
        private val collectionsRecordTypeListWorkMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellRecordType.WORK.value)
        ))

        private val collectionsRecordTypeListManifestMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellRecordType.MANIFESTATION.value)
        ))

        private val collectionsRecordTypeListItemMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellRecordType.ITEM.value)
        ))

        private val collectionsFormatListDigitalMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellFormat.DIGITAL.value)
        ))

        private val collectionsFormatListPhysicalMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellFormat.PHYSICAL.value)
        ))

        private val collectionsWorkTypeListSerialMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellDescriptionType.SERIAL.value)
        ))

        private val collectionsWorkTypeListYearMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = AxiellDescriptionType.YEAR.value)
        ))

        // Regular digital item
        private val collectionsPartsObjectMockItemA = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "4",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020.01.01")),
                recordType = collectionsRecordTypeListItemMock,
                workTypeList = null,
                formatList = collectionsFormatListDigitalMock,
                partsList = null
            )
        )

        // Regular physical item
        private val collectionsPartsObjectMockItemB = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "5",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020.01.01")),
                recordType = collectionsRecordTypeListItemMock,
                workTypeList = null,
                formatList = collectionsFormatListPhysicalMock,
                partsList = null
            )
        )

        // Regular manifestation with 2 child items
        private val collectionsPartsObjectMockManifestationA = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "3",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020.01.01")),
                recordType = collectionsRecordTypeListManifestMock,
                workTypeList = null,
                formatList = null,
                partsList = listOf(
                    collectionsPartsObjectMockItemA,
                    collectionsPartsObjectMockItemB
                )
            )
        )

        // Manifestation with no children
        private val collectionsPartsObjectMockManifestationB = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "9",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020.01.01")),
                recordType = collectionsRecordTypeListManifestMock,
                workTypeList = null,
                formatList = null,
                partsList = null
            )
        )

        // Regular year work with 1 manifestation child and 2 items
        private val collectionsPartsObjectMockYearWorkA = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "2",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020")),
                recordType = collectionsRecordTypeListWorkMock,
                workTypeList = collectionsWorkTypeListYearMock,
                formatList = null,
                partsList = listOf(collectionsPartsObjectMockManifestationA)
            )
        )

        // Year work without children
        private val collectionsPartsObjectMockYearWorkB = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "8",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2000")),
                recordType = collectionsRecordTypeListWorkMock,
                workTypeList = collectionsWorkTypeListYearMock,
                formatList = null,
                partsList = null
            )
        )

        // Year work with 1 manifestation and no items
        private val collectionsPartsObjectMockYearWorkC = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "10",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2000")),
                recordType = collectionsRecordTypeListWorkMock,
                workTypeList = collectionsWorkTypeListYearMock,
                formatList = null,
                partsList = listOf(collectionsPartsObjectMockManifestationB)
            )
        )

        // Regular title with 1 year work, 1 manifestation and 2 items
        val collectionsModelMockTitleA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "1",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(subMedium = "Avis")),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2020-01-01", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkA),
                        workTypeList = collectionsWorkTypeListSerialMock
                    )
                )
            )
        )

        // Title without any children
        val collectionsModelMockTitleB = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "6",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(subMedium = "Avis")),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = null,
                        workTypeList = collectionsWorkTypeListSerialMock
                    )
                )
            )
        )

        // Title with 1 year work and no manifestations
        val collectionsModelMockTitleC = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "7",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(subMedium = "Avis")),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkB),
                        workTypeList = collectionsWorkTypeListSerialMock
                    )
                )
            )
        )

        // Title with 1 year work, 1 manifestation and no items
        val collectionsModelMockTitleD = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "7",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(subMedium = "Avis")),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkC),
                        workTypeList = collectionsWorkTypeListSerialMock
                    )
                )
            )
        )

        val collectionsModelMockTitleE = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(CollectionsObject(
                    priRef = "2",
                    titleList = listOf(CollectionsTitle("Avis B")),
                    recordTypeList = listOf(listOf(CollectionsLanguageListObject("neutral", "WORK"))),
                    formatList = null,
                    partOfList = null,
                    subMediumList = listOf(SubMedium("Avis")),
                    mediumList = null,
                    datingList = listOf(CollectionsDating("2020-01-01", "2020-01-31")),
                    publisherList = listOf("B-Forlaget"),
                    languageList = null,
                    placeOfPublicationList = listOf("Brakka"),
                    partsList = null,
                    workTypeList = null
                ))
            )
        )

        val collectionsModelEmptyRecordListMock = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = null
            )
        )

        val collectionsPartOfObjectMockSerialWorkA = CollectionsPartOfObject(
            partOfReference = CollectionsPartOfReference(
                priRef = "22",
                partOfGroup = null,
                title = listOf(CollectionsTitle(title = "Bikubeavisen")),
                recordType = collectionsRecordTypeListWorkMock,
                subMedium = listOf(SubMedium(subMedium = "Avis")),
                workTypeList = collectionsWorkTypeListSerialMock
            )
        )

        private val collectionsPartOfObjectMockYearWorkA = CollectionsPartOfObject(
            partOfReference = CollectionsPartOfReference(
                priRef = "21",
                partOfGroup = listOf(collectionsPartOfObjectMockSerialWorkA),
                title = listOf(CollectionsTitle(title = "Bikubeavisen 1999")),
                recordType = collectionsRecordTypeListWorkMock,
                subMedium = null,
                workTypeList = collectionsWorkTypeListYearMock
            )
        )

        private val collectionsPartOfObjectMockManifestA = CollectionsPartOfObject(
            partOfReference = CollectionsPartOfReference(
                priRef = "20",
                partOfGroup = listOf(collectionsPartOfObjectMockYearWorkA),
                title = listOf(CollectionsTitle(title = "Bikubeavisen 1999.12.24")),
                recordType = collectionsRecordTypeListManifestMock,
                subMedium = null,
                workTypeList = null
            )
        )

        val collectionsModelMockItemA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "19",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 1999.12.24")),
                        recordTypeList = collectionsRecordTypeListItemMock,
                        formatList = collectionsFormatListDigitalMock,
                        partOfList = listOf(collectionsPartOfObjectMockManifestA),
                        subMediumList = null,
                        mediumList = null,
                        datingList = null,
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = null,
                        workTypeList = null
                    )
                )
            )
        )

        val collectionsModelMockManifestationA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "24",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 1999.12.24")),
                        recordTypeList = collectionsRecordTypeListManifestMock,
                        formatList = null,
                        partOfList = listOf(collectionsPartOfObjectMockYearWorkA),
                        subMediumList = null,
                        mediumList = null,
                        datingList = null,
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = listOf(collectionsPartsObjectMockItemA),
                        workTypeList = null
                    )
                )
            )
        )
        val collectionsModelMockYearWorkA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "25",
                        titleList = listOf(CollectionsTitle(title = "Bikubeavisen 1999")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = listOf(collectionsPartOfObjectMockSerialWorkA),
                        subMediumList = null,
                        mediumList = null,
                        datingList = null,
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = listOf(collectionsPartsObjectMockManifestationA),
                        workTypeList = collectionsWorkTypeListYearMock
                    )
                )
            )
        )
    }
}
