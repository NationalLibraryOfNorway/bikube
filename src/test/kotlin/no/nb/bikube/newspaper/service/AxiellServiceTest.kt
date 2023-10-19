package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockItemB
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
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.collections.*
import no.nb.bikube.core.model.dto.ItemDto
import no.nb.bikube.core.model.dto.ManifestationDto
import no.nb.bikube.core.model.dto.TitleDto
import no.nb.bikube.core.model.dto.YearDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperInputDtoItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@SpringBootTest
@ActiveProfiles("test")
class AxiellServiceTest(
    @Autowired private val globalControllerExceptionHandler: GlobalControllerExceptionHandler
) {
    @Autowired
    private lateinit var axiellService: AxiellService

    @MockkBean
    private lateinit var axiellRepository: AxiellRepository

    private val yearWorkEncodedDto = Json.encodeToString(YearDto(
        partOfReference = newspaperItemMockB.titleCatalogueId,
        recordType = AxiellRecordType.WORK.value,
        descriptionType = AxiellDescriptionType.YEAR.value,
        dateStart = newspaperTitleMockB.startDate.toString().take(4),
        title = newspaperTitleMockB.startDate.toString().take(4),
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    ))

    private val manifestationEncodedDto = Json.encodeToString(ManifestationDto(
        partOfReference = newspaperItemMockB.catalogueId,
        recordType = AxiellRecordType.MANIFESTATION.value,
        dateStart = newspaperItemMockB.date.toString(),
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    ))

    private val itemEncodedDto = Json.encodeToString(ItemDto(
        format = AxiellFormat.DIGITAL.value,
        recordType = AxiellRecordType.ITEM.value,
        altNumber = newspaperItemMockB.urn,
        altNumberType = "URN",
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId
    ))

    private val itemEncodedDtoPhysical = Json.encodeToString(ItemDto(
        format = AxiellFormat.PHYSICAL.value,
        recordType = AxiellRecordType.ITEM.value,
        altNumber = null,
        altNumberType = null,
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId)
    )

    @BeforeEach
    fun beforeEach() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(9, 30, 0)
    }

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(9, 30, 0)
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)

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
                medium = "Tekst",
                subMedium = newspaperTitleMockB.materialType,
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        axiellService.createNewspaperTitle(body)
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(encodedValue) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.error(AxiellCollectionsException("Error creating title"))

        axiellService.createNewspaperTitle(newspaperTitleMockB)
            .test()
            .expectErrorMatches { it is AxiellCollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getItemsForTitle should return all items for title`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleA.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return title information on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record: CollectionsObject = collectionsModelMockTitleA.getFirstObject()!!
        axiellService.getItemsForTitle(record.priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.titleCatalogueId == record.priRef
                && it.titleName == record.getName()
            }
            .expectNextMatches {
                it.titleCatalogueId == record.priRef
                && it.titleName == record.getName()
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return material type on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record = collectionsModelMockTitleA.getFirstObject()!!
        axiellService.getItemsForTitle(record.priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.materialType == record.getMaterialType()!!.norwegian
            }
            .expectNextMatches {
                it.materialType == record.getMaterialType()!!.norwegian
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when serial work has no year works`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleB.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleB.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when year work has no manifestations`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleC.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleC.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when manifestation has no items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleD.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleD.getFirstObject()!!.priRef)
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
        val testRecord = collectionsModelMockItemA.getFirstObject()!!
        val testSerialWork = collectionsPartOfObjectMockSerialWorkA.partOfReference!!

        axiellService.getSingleItem("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Item(
                        catalogueId = testRecord.priRef,
                        name = testRecord.getName(),
                        date = testRecord.getItemDate(),
                        materialType = testSerialWork.getMaterialType()!!.norwegian,
                        titleCatalogueId = testSerialWork.priRef,
                        titleName = testSerialWork.getName(),
                        digital = true,
                        urn = testRecord.getUrn()
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
                    collectionsModelMockItemA.getFirstObject()!!.copy(),
                    collectionsModelMockItemA.getFirstObject()!!.copy()
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
        val testRecord = collectionsModelMockTitleA.getFirstObject()!!

        axiellService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Title(
                        name = testRecord.getName(),
                        startDate = testRecord.getStartDate(),
                        endDate = null,
                        publisher = testRecord.getPublisher(),
                        publisherPlace = testRecord.getPublisherPlace(),
                        language = testRecord.getLanguage(),
                        materialType = testRecord.getMaterialType()!!.norwegian,
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
                    collectionsModelMockTitleA.getFirstObject()!!.copy(),
                    collectionsModelMockTitleB.getFirstObject()!!.copy()
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
                        name = collectionsModelMockTitleA.getFirstObject()!!.getName(),
                        startDate = collectionsModelMockTitleA.getFirstObject()!!.getStartDate(),
                        endDate = null,
                        publisher = collectionsModelMockTitleA.getFirstObject()!!.getPublisher(),
                        publisherPlace = collectionsModelMockTitleA.getFirstObject()!!.getPublisherPlace(),
                        language = collectionsModelMockTitleA.getFirstObject()!!.getLanguage(),
                        materialType = collectionsModelMockTitleA.getFirstObject()!!.getMaterialType()!!.norwegian,
                        catalogueId = collectionsModelMockTitleA.getFirstObject()!!.priRef
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
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)
        axiellService.createNewspaperTitle(newspaperTitleMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `createTitle should correctly encode the title object sent to json string`() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(9, 30, 0)
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)
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
                medium = "Tekst",
                subMedium = newspaperTitleMockB.materialType,
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        axiellService.createNewspaperTitle(newspaperTitleMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(encodedValue) }
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
        val expectedName = collectionsNameModelMockA.getFirstObject()!!.name
        val expectedId = collectionsNameModelMockA.getFirstObject()!!.priRef
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
        val publisherPlaceName = collectionsTermModelMockLocationB.getFirstObject()!!.term
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
        val expectedTerm = collectionsTermModelMockLocationB.getFirstObject()!!.term
        val expectedId = collectionsTermModelMockLocationB.getFirstObject()!!.priRef
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
        val expectedTerm = collectionsTermModelMockLanguageA.getFirstObject()!!.term
        val expectedId = collectionsTermModelMockLanguageA.getFirstObject()!!.priRef
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

    @Test
    fun `createNewspaperItem should return correctly mapped item record`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        axiellService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null), it) }
            .verifyComplete()
    }

    @Test
    fun `createNewspaperItem should correctly encode the item object sent to json string`() {
        every { axiellRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.createTextsRecord(yearWorkEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        axiellService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null), it) }
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(itemEncodedDto) }
    }

    @Test
    fun `createNewspaperItem should throw AxiellItemNotFound if item could not be found`() {
        every { axiellRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelEmptyRecordListMock)
        every { axiellRepository.createTextsRecord(yearWorkEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        axiellService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is AxiellItemNotFound &&
                it.message!!.contains("New item not found")
            }
            .verify()
    }

    @Test
    fun `createNewspaperItem should ignore URN if is is a physical item`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        axiellService.createNewspaperItem(newspaperInputDtoItemMockB.copy(digital = false))
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(itemEncodedDtoPhysical) }
    }

    @Test
    fun `createManifestation should throw AxiellManifestationNotFound if manifestation could not be found`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        axiellService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is AxiellManifestationNotFound &&
                it.message!!.contains("New manifestation not found")
            }
            .verify()
    }

    @Test
    fun `createManifestation should return correctly mapped manifestation record`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        axiellService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject()!!, it)
            }
            .verifyComplete()
    }

    @Test
    fun `createManifestation should correctly encode the manifestation object sent to json string`() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(9, 30, 0)
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        val encodedValue = Json.encodeToString(
            ManifestationDto(
                partOfReference = "1",
                recordType = AxiellRecordType.MANIFESTATION.value,
                dateStart = LocalDate.now().toString(),
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        axiellService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject()!!, it)
            }
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(encodedValue) }
    }

    @Test
    fun `createYearWork should throw AxiellYearWorkNotFound if year work could not be found`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        axiellService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is AxiellYearWorkNotFound && it.message!!.contains("New year not found")
            }
            .verify()
    }

    @Test
    fun `createYearWork should return correctly mapped year work record`() {
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockYearWorkA)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA)

        axiellService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockYearWorkA.getFirstObject()!!, it)
            }
            .verifyComplete()
    }

    @Test
    fun `createYearWork should correctly encode the year work object sent to json string`() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns LocalTime.of(9, 30, 0)
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockYearWorkA)
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA)

        val encodedValue = Json.encodeToString(
            YearDto(
                partOfReference = "1",
                recordType = AxiellRecordType.WORK.value,
                descriptionType = AxiellDescriptionType.YEAR.value,
                dateStart = "2023",
                title = "2023",
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        axiellService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockYearWorkA.getFirstObject()!!, it)
            }
            .verifyComplete()

        verify { axiellRepository.createTextsRecord(encodedValue) }
    }
}
