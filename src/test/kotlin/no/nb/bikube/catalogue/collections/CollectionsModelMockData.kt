package no.nb.bikube.catalogue.collections

import no.nb.bikube.catalogue.collections.enum.CollectionsDescriptionType
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.core.enum.MaterialType

class CollectionsModelMockData {
    companion object {
        const val TEST_USERNAME = "bikube-test"
        const val INPUT_NOTES = "Registrert i Bikube"

        private val collectionsRecordTypeListWorkMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsRecordType.WORK.value)
        ))

        private val collectionsRecordTypeListManifestMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsRecordType.MANIFESTATION.value)
        ))

        private val collectionsRecordTypeListItemMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsRecordType.ITEM.value)
        ))

        private val collectionsFormatListDigitalMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsFormat.DIGITAL.value)
        ))

        private val collectionsFormatListPhysicalMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsFormat.PHYSICAL.value)
        ))

        private val collectionsWorkTypeListSerialMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsDescriptionType.SERIAL.value)
        ))

        private val collectionsWorkTypeListYearMock = listOf(listOf(
            CollectionsLanguageListObject(lang = "neutral", text = CollectionsDescriptionType.YEAR.value)
        ))

        // Regular digital item
        val collectionsPartsObjectMockItemA = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "4",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen 2020.01.01")),
                recordType = collectionsRecordTypeListItemMock,
                workTypeList = null,
                formatList = collectionsFormatListDigitalMock,
                partsList = null,
                dateStart = null
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
                partsList = null,
                dateStart = null
            )
        )

        // Regular item without date in title
        val collectionsPartsObjectMockItemC = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "5",
                titleList = listOf(CollectionsTitle(title = "Bikubeavisen")),
                recordType = collectionsRecordTypeListItemMock,
                workTypeList = null,
                formatList = collectionsFormatListPhysicalMock,
                partsList = null,
                dateStart = null
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
                ),
                dateStart = listOf(CollectionsDating(dateFrom = "2020-01-01", dateTo = null))
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
                partsList = null,
                dateStart = listOf(CollectionsDating(dateFrom = "2020-01-01", dateTo = null))
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
                partsList = listOf(collectionsPartsObjectMockManifestationA),
                dateStart = listOf(CollectionsDating(dateFrom = "2020", dateTo = null))
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
                partsList = null,
                dateStart = listOf(CollectionsDating(dateFrom = "2000", dateTo = null))
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
                partsList = listOf(collectionsPartsObjectMockManifestationB),
                dateStart = null
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
                        subMediumList = listOf(SubMedium(subMedium = MaterialType.NEWSPAPER.norwegian)),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2020-01-01", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkA),
                        workTypeList = collectionsWorkTypeListSerialMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
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
                        subMediumList = listOf(SubMedium(subMedium = MaterialType.NEWSPAPER.norwegian)),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = null,
                        workTypeList = collectionsWorkTypeListSerialMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
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
                        subMediumList = listOf(SubMedium(subMedium = MaterialType.NEWSPAPER.norwegian)),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkB),
                        workTypeList = collectionsWorkTypeListSerialMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
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
                        subMediumList = listOf(SubMedium(subMedium = MaterialType.NEWSPAPER.norwegian)),
                        mediumList = listOf(Medium(medium = "Tekst")),
                        datingList = listOf(CollectionsDating(dateFrom = "2000", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = listOf(collectionsPartsObjectMockYearWorkC),
                        workTypeList = collectionsWorkTypeListSerialMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        // Equal to newspaperTitleMockB
        val collectionsModelMockTitleE = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "2",
                        titleList = listOf(CollectionsTitle("Avis B")),
                        recordTypeList = listOf(listOf(CollectionsLanguageListObject("neutral", "WORK"))),
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(MaterialType.NEWSPAPER.norwegian)),
                        mediumList = null,
                        datingList = listOf(CollectionsDating("2020-01-01", "2020-01-31")),
                        publisherList = listOf("B-Forlaget"),
                        languageList = listOf(CollectionsLanguage("nob")),
                        placeOfPublicationList = listOf("Brakka"),
                        partsList = null,
                        workTypeList = collectionsWorkTypeListSerialMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        val collectionsModelEmptyRecordListMock = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = null
            )
        )

        val collectionsModelMockAllTitles = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockTitleA.getFirstObject()!!,
                    collectionsModelMockTitleB.getFirstObject()!!,
                    collectionsModelMockTitleC.getFirstObject()!!,
                    collectionsModelMockTitleD.getFirstObject()!!,
                    collectionsModelMockTitleE.getFirstObject()!!
                )
            )
        )

        val collectionsPartOfObjectMockSerialWorkA = CollectionsPartOfObject(
            partOfReference = CollectionsPartOfReference(
                priRef = "22",
                partOfGroup = null,
                title = listOf(CollectionsTitle(title = "Bikubeavisen")),
                recordType = collectionsRecordTypeListWorkMock,
                subMedium = listOf(SubMedium(subMedium = MaterialType.NEWSPAPER.norwegian)),
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
                        datingList = listOf(CollectionsDating(dateFrom = "1999-12-24", dateTo = null)),
                        publisherList = listOf("NB"),
                        languageList = listOf(CollectionsLanguage(language = "nob")),
                        placeOfPublicationList = listOf("Mo I Rana"),
                        partsList = null,
                        workTypeList = null,
                        alternativeNumberList = listOf(CollectionsAlternativeNumber("URN", "bikubeavisen_null_null_19991224_1_1_1")),
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        // Equals to newspaperItemMockB, except title mapping
        val collectionsModelMockItemB = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "46",
                        titleList = listOf(CollectionsTitle(title = "Avis A 2020.01.05")),
                        recordTypeList = collectionsRecordTypeListItemMock,
                        formatList = collectionsFormatListDigitalMock,
                        partOfList = null,
                        subMediumList = null,
                        mediumList = null,
                        datingList = null,
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = null,
                        workTypeList = null,
                        alternativeNumberList = listOf(CollectionsAlternativeNumber("URN", "avisa_null_null_20200105_1_1_1")),
                        inputName = TEST_USERNAME
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
                        workTypeList = null,
                        alternativeNumberList = listOf(CollectionsAlternativeNumber("URN", "bikubeavisen_null_null_19991224_1_1_1")),
                        inputName = TEST_USERNAME
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
                        workTypeList = collectionsWorkTypeListYearMock,
                        alternativeNumberList = listOf(CollectionsAlternativeNumber("URN", "bikubeavisen_null_null_19991224_1_1_1")),
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        // No manifestation children
        val collectionsModelMockYearWorkB = CollectionsModel(
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
                        partsList = null,
                        workTypeList = collectionsWorkTypeListYearMock,
                        alternativeNumberList = listOf(CollectionsAlternativeNumber("URN", "bikubeavisen_null_null_19991224_1_1_1")),
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        val collectionsNameModelMockA = CollectionsNameModel(
            adlibJson = CollectionsNameRecordList(
                recordList = listOf(
                    CollectionsNameObject(
                        priRef = "123",
                        name = "Schibsted",
                    )
                )
            )
        )

        val collectionsTermModelMockLanguageA = CollectionsTermModel(
            adlibJson = CollectionsTermRecordList(
                recordList = listOf(
                    CollectionsTermObject(
                        priRef = "123",
                        term = "nob",
                    )
                )
            )
        )

        val collectionsTermModelMockLocationB = CollectionsTermModel(
            adlibJson = CollectionsTermRecordList(
                recordList = listOf(
                    CollectionsTermObject(
                        priRef = "123",
                        term = "Oslo",
                    )
                )
            )
        )

        val collectionsTermModelWithEmptyRecordListA = CollectionsTermModel(
            adlibJson = CollectionsTermRecordList(recordList = null)
        )

        val collectionsNameModelWithEmptyRecordListA = CollectionsNameModel(
            adlibJson = CollectionsNameRecordList(recordList = null)
        )

        val collectionsModelMockYearWorkC = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "30",
                        titleList = listOf(CollectionsTitle(title = "Aftenposten")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = null,
                        mediumList = null,
                        datingList = listOf(CollectionsDating(dateFrom = "2020-01-01", dateTo = null)),
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = listOf(collectionsPartsObjectMockManifestationB),
                        workTypeList = collectionsWorkTypeListYearMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        private val collectionsPartsObjectMockItemD = CollectionsPartsObject(
            partsReference = CollectionsPartsReference(
                priRef = "32",
                titleList = listOf(CollectionsTitle(title = "Aftenposten")),
                recordType = collectionsRecordTypeListItemMock,
                workTypeList = null,
                formatList = collectionsFormatListDigitalMock,
                partsList = null,
                dateStart = null
            )
        )

        val collectionsModelMockManifestationC = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "31",
                        titleList = listOf(CollectionsTitle(title = "Aftenposten")),
                        recordTypeList = collectionsRecordTypeListManifestMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = null,
                        mediumList = null,
                        datingList = null,
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = listOf(collectionsPartsObjectMockItemD),
                        workTypeList = null,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
                    )
                )
            )
        )

        val yearWorkNoManifestationA = CollectionsModel(
            adlibJson = CollectionsRecordList(
                recordList = listOf(
                    CollectionsObject(
                        priRef = "100",
                        titleList = listOf(CollectionsTitle(title = "Aftenposten")),
                        recordTypeList = collectionsRecordTypeListWorkMock,
                        formatList = null,
                        partOfList = null,
                        subMediumList = listOf(SubMedium(subMedium = "Aviser")),
                        mediumList = null,
                        datingList = listOf(CollectionsDating(dateFrom = "2024", dateTo = null)),
                        publisherList = null,
                        languageList = null,
                        placeOfPublicationList = null,
                        partsList = listOf(),
                        workTypeList = collectionsWorkTypeListYearMock,
                        alternativeNumberList = null,
                        inputName = TEST_USERNAME
                    )
                )
            )
        )
    }
}
