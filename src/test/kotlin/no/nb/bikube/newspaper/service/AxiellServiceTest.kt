package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleC
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleD
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleE
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockYearWorkA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsNameModelMockA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsNameModelWithEmptyRecordListA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsPartOfObjectMockSerialWorkA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.dto.TitleDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class AxiellServiceTest(
    @Autowired private val globalControllerExceptionHandler: GlobalControllerExceptionHandler
) {
    @Autowired
    private lateinit var axiellService: AxiellService

    @MockkBean
    private lateinit var axiellRepository: AxiellRepository

    @Test
    fun `getTitle should fetch data as CollectionModel from repo and convert to title`() {
        every { axiellRepository.getAllTitles() } returns Mono.just(collectionsModelMockTitleE)

        axiellService.getTitles()
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        every { axiellRepository.createRecord(any()) } returns Mono.just(collectionsModelMockTitleE)

        val body = newspaperTitleMockB.copy()
        val encodedValue = Json.encodeToString(
            TitleDto(
            title = newspaperTitleMockB.name!!,
            dateStart = newspaperTitleMockB.startDate.toString(),
            dateEnd = newspaperTitleMockB.endDate.toString(),
            publisher = newspaperTitleMockB.publisher,
            placeOfPublication = newspaperTitleMockB.publisherPlace,
            language = newspaperTitleMockB.language,
            recordType = AxiellRecordType.WORK.value,
            descriptionType = AxiellDescriptionType.SERIAL.value,
            subMedium = newspaperTitleMockB.materialType
        )
        )

        axiellService.createTitle(body)
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()

        verify { axiellRepository.createRecord(encodedValue) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { axiellRepository.createRecord(any()) } returns Mono.error(AxiellCollectionsException("Error creating title"))

        axiellService.createTitle(newspaperTitleMockB)
            .test()
            .expectErrorMatches { it is AxiellCollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getItemsForTitle should return all items for title`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleA.adlibJson.recordList!!.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return title information on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record: CollectionsObject = collectionsModelMockTitleA.adlibJson.recordList!!.first() as CollectionsObject
        axiellService.getItemsForTitle(record.priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.titleCatalogueId == record.priRef
                        && it.titleName == record.titleList!!.first().title
            }
            .expectNextMatches {
                it.titleCatalogueId == record.priRef
                        && it.titleName == record.titleList!!.first().title
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return material type on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record = collectionsModelMockTitleA.adlibJson.recordList!!.first()
        axiellService.getItemsForTitle(record.priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.materialType == record.subMediumList!!.first().subMedium
            }
            .expectNextMatches {
                it.materialType == record.subMediumList!!.first().subMedium
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when serial work has no year works`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleB.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleB.adlibJson.recordList!!.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when year work has no manifestations`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleC.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleC.adlibJson.recordList!!.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when manifestation has no items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleD.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleD.adlibJson.recordList!!.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return an error if title does not exist`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        axiellService.getItemsForTitle("123")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is AxiellTitleNotFound &&
                    globalControllerExceptionHandler.handleAxiellTitleNotFoundException(it).status == 404
            }
            .verify()
    }

    @Test
    fun `getSingleItem should return correctly mapped item`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemA.copy())
        val testRecord = collectionsModelMockItemA.adlibJson.recordList!!.first()
        val testSerialWork = collectionsPartOfObjectMockSerialWorkA.partOfReference!!

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Item(
                        catalogueId = testRecord.priRef,
                        name = testRecord.titleList!!.first().title,
                        date = LocalDate.parse(testRecord.titleList!!.first().title!!.takeLast(10).replace(".", "-")),
                        materialType = testSerialWork.subMedium!!.first().subMedium,
                        titleCatalogueId = testSerialWork.priRef,
                        titleName = testSerialWork.title!!.first().title,
                        digital = true
                    ),
                    it
                )
            }
            .verifyComplete()
    }

    @Test
    fun `getSingleItem should throw AxiellTitleNotFound if object is a manifestation`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA.copy())

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw AxiellTitleNotFound if object is a work`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA.copy())

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw AxiellTitleNotFound if no items are received`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw AxiellCollectionsException if multiple items are received`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockItemA.adlibJson.recordList!!.first().copy(),
                    collectionsModelMockItemA.adlibJson.recordList!!.first().copy()
                )
            ))
        )

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(AxiellCollectionsException::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should return correctly mapped title`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())
        val testRecord = collectionsModelMockTitleA.adlibJson.recordList!!.first()

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Title(
                        name = testRecord.titleList!!.first().title,
                        startDate = LocalDate.parse(testRecord.datingList!!.first().dateFrom),
                        endDate = null,
                        publisher = testRecord.publisherList!!.first(),
                        publisherPlace = testRecord.placeOfPublicationList!!.first(),
                        language = testRecord.languageList!!.first().language,
                        materialType = testRecord.subMediumList!!.first().subMedium,
                        catalogueId = testRecord.priRef
                    ),
                    it
                )
            }
            .verifyComplete()
    }

    @Test
    fun `getSingleTitle should throw AxiellTitleNotFound if object is a manifestation`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA.copy())

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw AxiellTitleNotFound if object is an item`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemA.copy())

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw AxiellTitleNotFound if object is a year work`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA.copy())

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw AxiellTitleNotFound if no items are received`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(AxiellTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw AxiellCollectionsException if multiple items are received`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockTitleA.adlibJson.recordList!!.first().copy(),
                    collectionsModelMockTitleB.adlibJson.recordList!!.first().copy()
                )
            ))
        )

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(AxiellCollectionsException::class.java)
            .verify()
    }

    @Test
    fun `getTitleByName should return correctly mapped title`() {
        every { axiellRepository.getTitleByName(any()) } returns Mono.just(collectionsModelMockTitleA)

        axiellService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Title(
                        name = collectionsModelMockTitleA.adlibJson.recordList!!.first().titleList!!.first().title,
                        startDate = LocalDate.parse(collectionsModelMockTitleA.adlibJson.recordList!!.first().datingList!!.first().dateFrom),
                        endDate = null,
                        publisher = collectionsModelMockTitleA.adlibJson.recordList!!.first().publisherList!!.first(),
                        publisherPlace = collectionsModelMockTitleA.adlibJson.recordList!!.first().placeOfPublicationList!!.first(),
                        language = collectionsModelMockTitleA.adlibJson.recordList!!.first().languageList!!.first().language,
                        materialType = collectionsModelMockTitleA.adlibJson.recordList!!.first().subMediumList!!.first().subMedium,
                        catalogueId = collectionsModelMockTitleA.adlibJson.recordList!!.first().priRef
                    ),
                    it
                )
            }
            .verifyComplete()
    }

    @Test
    fun `getTitleByName should return empty Mono if no titles are found`() {
        every { axiellRepository.getTitleByName(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        axiellService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createTitle should return correctly mapped record`() {
        every { axiellRepository.createRecord(any()) } returns Mono.just(collectionsModelMockTitleE)
        axiellService.createTitle(newspaperTitleMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `createTitle should correctly encode the title object sent to json string`() {
        every { axiellRepository.createRecord(any()) } returns Mono.just(collectionsModelMockTitleE)
        val encodedValue = Json.encodeToString(
            TitleDto(
                title = newspaperTitleMockB.name!!,
                dateStart = newspaperTitleMockB.startDate.toString(),
                dateEnd = newspaperTitleMockB.endDate.toString(),
                publisher = newspaperTitleMockB.publisher,
                placeOfPublication = newspaperTitleMockB.publisherPlace,
                language = newspaperTitleMockB.language,
                recordType = AxiellRecordType.WORK.value,
                descriptionType = AxiellDescriptionType.SERIAL.value,
                subMedium = newspaperTitleMockB.materialType
            )
        )

        axiellService.createTitle(newspaperTitleMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()

        verify { axiellRepository.createRecord(encodedValue) }
    }

    @Test
    fun `searchTitleByName should return a correctly mapped catalogue record`() {
        every { axiellRepository.getTitleByName(any()) } returns Mono.just(collectionsModelMockTitleE)
        axiellService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `searchTitleByName should return a flux of an empty list if no titles are found`() {
        every { axiellRepository.getTitleByName(any()) } returns Mono.empty()
        axiellService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createPublisher should return RecordAlreadyExistsException if searchPublisher returns non-empty list`() {
        every { axiellRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA)
        axiellService.createPublisher("1")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Publisher '1' already exists" }
            .verify()
    }

    @Test
    fun `createPublisher should call createRecord if search returns empty recordList`() {
        every { axiellRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelWithEmptyRecordListA)
        every { axiellRepository.createNameRecord(any(), any()) } returns Mono.just(collectionsNameModelMockA)
        val expectedName = collectionsNameModelMockA.adlibJson.recordList!!.first().name
        val expectedId = collectionsNameModelMockA.adlibJson.recordList!!.first().priRef
        axiellService.createPublisher("Schibsted")
            .test()
            .expectNext(Publisher(expectedName, expectedId))
            .verifyComplete()

        verify { axiellRepository.createNameRecord(any(), any()) }
    }

    @Test
    fun `createPublisher should throw BadRequestBodyException if publisher is empty`() {
        assertThrows<BadRequestBodyException> { axiellService.createPublisher("") }
    }

    @Test
    fun `createPublisherPlace should return RecordAlreadyExistsException if searchPublisherPlace returns non-empty list`() {
        every { axiellRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB)
        val publisherPlaceName = collectionsTermModelMockLocationB.adlibJson.recordList!!.first().term
        axiellService.createPublisherPlace(publisherPlaceName)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is RecordAlreadyExistsException &&
                it.message == "Publisher place '$publisherPlaceName' already exists"
            }
            .verify()
    }

    @Test
    fun `createPublisherPlace should call createRecord if search returns empty recordList`() {
        every { axiellRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA)
        every { axiellRepository.createTermRecord(any(), any()) } returns Mono.just(collectionsTermModelMockLocationB)
        val expectedTerm = collectionsTermModelMockLocationB.adlibJson.recordList!!.first().term
        val expectedId = collectionsTermModelMockLocationB.adlibJson.recordList!!.first().priRef
        axiellService.createPublisherPlace("Oslo")
            .test()
            .expectNext(PublisherPlace(expectedTerm, expectedId))
            .verifyComplete()

        verify { axiellRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createPublisherPlace should throw BadRequestBodyException if publisher place is empty`() {
        assertThrows<BadRequestBodyException> { axiellService.createPublisherPlace("") }
    }

    @Test
    fun `createLanguage should return RecordAlreadyExistsException if searchLanguage returns non-empty list`() {
        every { axiellRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        axiellService.createLanguage("nob")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Language 'nob' already exists" }
            .verify()
    }

    @Test
    fun `createLanguage should call createRecord if search returns empty recordList`() {
        every { axiellRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA)
        every { axiellRepository.createTermRecord(any(), any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        val expectedTerm = collectionsTermModelMockLanguageA.adlibJson.recordList!!.first().term
        val expectedId = collectionsTermModelMockLanguageA.adlibJson.recordList!!.first().priRef
        axiellService.createLanguage("nob")
            .test()
            .expectNext(Language(expectedTerm, expectedId))
            .verifyComplete()

        verify { axiellRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createLanguage should throw BadRequestBodyException if language code is not a valid ISO-639-2 language code`() {
        assertThrows<BadRequestBodyException> { axiellService.createLanguage("") }
        assertThrows<BadRequestBodyException> { axiellService.createLanguage("en") }
        assertThrows<BadRequestBodyException> { axiellService.createLanguage("english") }
    }
}
