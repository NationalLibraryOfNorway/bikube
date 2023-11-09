package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
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
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsPartsObjectMockItemA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA
import no.nb.bikube.core.enum.CollectionsDescriptionType
import no.nb.bikube.core.enum.CollectionsFormat
import no.nb.bikube.core.enum.CollectionsRecordType
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.*
import no.nb.bikube.core.model.*
import no.nb.bikube.core.model.collections.*
import no.nb.bikube.core.model.dto.ItemDto
import no.nb.bikube.core.model.dto.ManifestationDto
import no.nb.bikube.core.model.dto.TitleDto
import no.nb.bikube.core.model.dto.YearDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperInputDtoItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.repository.CollectionsRepository
import org.junit.jupiter.api.*
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
class CollectionsServiceTest(
    @Autowired private val globalControllerExceptionHandler: GlobalControllerExceptionHandler
) {

    companion object {
        @JvmStatic
        @AfterAll
        fun unmockLocalTime() {
            unmockkStatic(LocalTime::class)
        }
    }

    @Autowired
    private lateinit var collectionsService: CollectionsService

    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    private val mockedTime = LocalTime.of(9, 30, 0)

    @BeforeEach
    fun mockLocalTime() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns mockedTime
    }

    private val yearWorkEncodedDto = Json.encodeToString(YearDto(
        partOfReference = newspaperItemMockB.titleCatalogueId,
        recordType = CollectionsRecordType.WORK.value,
        descriptionType = CollectionsDescriptionType.YEAR.value,
        dateStart = newspaperTitleMockB.startDate.toString().take(4),
        title = newspaperTitleMockB.startDate.toString().take(4),
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    ))

    private val manifestationEncodedDto = Json.encodeToString(ManifestationDto(
        partOfReference = newspaperItemMockB.catalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        dateStart = newspaperItemMockB.date.toString(),
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    ))

    private val itemEncodedDto = Json.encodeToString(ItemDto(
        format = CollectionsFormat.DIGITAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        altNumber = newspaperItemMockB.urn,
        altNumberType = "URN",
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId
    ))

    private val itemEncodedDtoPhysical = Json.encodeToString(ItemDto(
        format = CollectionsFormat.PHYSICAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        altNumber = null,
        altNumberType = null,
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId)
    )

    private val titleEncodedDto = Json.encodeToString(TitleDto(
        title = newspaperTitleMockB.name!!,
        dateStart = newspaperTitleMockB.startDate.toString(),
        dateEnd = newspaperTitleMockB.endDate.toString(),
        publisher = newspaperTitleMockB.publisher,
        placeOfPublication = newspaperTitleMockB.publisherPlace,
        language = newspaperTitleMockB.language,
        recordType = CollectionsRecordType.WORK.value,
        descriptionType = CollectionsDescriptionType.SERIAL.value,
        medium = "Tekst",
        subMedium = newspaperTitleMockB.materialType,
        inputName = "Bikube API",
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts")
    )

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)

        val body = newspaperTitleInputDtoMockB.copy()

        collectionsService.createNewspaperTitle(body)
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(titleEncodedDto) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.error(CollectionsException("Error creating title"))

        collectionsService.createNewspaperTitle(newspaperTitleInputDtoMockB)
            .test()
            .expectErrorMatches { it is CollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getItemsForTitle should return all items for title`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        collectionsService.getItemsForTitle(collectionsModelMockTitleA.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return title information on items`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record: CollectionsObject = collectionsModelMockTitleA.getFirstObject()!!
        collectionsService.getItemsForTitle(record.priRef)
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
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        val record = collectionsModelMockTitleA.getFirstObject()!!
        collectionsService.getItemsForTitle(record.priRef)
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
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleB.copy())

        collectionsService.getItemsForTitle(collectionsModelMockTitleB.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when year work has no manifestations`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleC.copy())

        collectionsService.getItemsForTitle(collectionsModelMockTitleC.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when manifestation has no items`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleD.copy())

        collectionsService.getItemsForTitle(collectionsModelMockTitleD.getFirstObject()!!.priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return an error if title does not exist`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        collectionsService.getItemsForTitle("123")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is CollectionsTitleNotFound &&
                    globalControllerExceptionHandler.handleCollectionsTitleNotFoundException(it).status == 404
            }
            .verify()
    }

    @Test
    fun `getSingleItem should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemA.copy())
        val testRecord = collectionsModelMockItemA.getFirstObject()!!
        val testSerialWork = collectionsPartOfObjectMockSerialWorkA.partOfReference!!

        collectionsService.getSingleItem("1")
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
    fun `getSingleItem should throw CollectionsTitleNotFound if object is a manifestation`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA.copy())

        collectionsService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if object is a work`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA.copy())

        collectionsService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        collectionsService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsException if multiple items are received`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockItemA.getFirstObject()!!.copy(),
                    collectionsModelMockItemA.getFirstObject()!!.copy()
                )
            ))
        )

        collectionsService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should return correctly mapped title`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())
        val testRecord = collectionsModelMockTitleA.getFirstObject()!!

        collectionsService.getSingleTitle("1")
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
    fun `getSingleTitle should throw CollectionsTitleNotFound if object is a manifestation`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA.copy())

        collectionsService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if object is an item`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemA.copy())

        collectionsService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if object is a year work`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA.copy())

        collectionsService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        collectionsService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsException if multiple items are received`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockTitleA.getFirstObject()!!.copy(),
                    collectionsModelMockTitleB.getFirstObject()!!.copy()
                )
            ))
        )

        collectionsService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `getTitleByName should return correctly mapped title`() {
        every { collectionsRepository.getTitleByName(any()) } returns Mono.just(collectionsModelMockTitleA)

        collectionsService.searchTitleByName("1")
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
        every { collectionsRepository.getTitleByName(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        collectionsService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createTitle should return correctly mapped record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)
        collectionsService.createNewspaperTitle(newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `createTitle should correctly encode the title object sent to json string`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE)

        collectionsService.createNewspaperTitle(newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(titleEncodedDto) }
    }

    @Test
    fun `searchTitleByName should return a correctly mapped catalogue record`() {
        every { collectionsRepository.getTitleByName(any()) } returns Mono.just(collectionsModelMockTitleE)
        collectionsService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `searchTitleByName should return a flux of an empty list if no titles are found`() {
        every { collectionsRepository.getTitleByName(any()) } returns Mono.empty()
        collectionsService.searchTitleByName("1")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createPublisher should return RecordAlreadyExistsException if searchPublisher returns non-empty list`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA)
        collectionsService.createPublisher("1")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Publisher '1' already exists" }
            .verify()
    }

    @Test
    fun `createPublisher should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelWithEmptyRecordListA)
        every { collectionsRepository.createNameRecord(any(), any()) } returns Mono.just(collectionsNameModelMockA)
        val expectedName = collectionsNameModelMockA.getFirstObject()!!.name
        val expectedId = collectionsNameModelMockA.getFirstObject()!!.priRef
        collectionsService.createPublisher("Schibsted")
            .test()
            .expectNext(Publisher(expectedName, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createNameRecord(any(), any()) }
    }

    @Test
    fun `createPublisher should throw BadRequestBodyException if publisher is empty`() {
        assertThrows<BadRequestBodyException> { collectionsService.createPublisher("") }
    }

    @Test
    fun `createPublisherPlace should return RecordAlreadyExistsException if searchPublisherPlace returns non-empty list`() {
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB)
        val publisherPlaceName = collectionsTermModelMockLocationB.getFirstObject()!!.term
        collectionsService.createPublisherPlace(publisherPlaceName)
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
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA)
        every { collectionsRepository.createTermRecord(any(), any()) } returns Mono.just(collectionsTermModelMockLocationB)
        val expectedTerm = collectionsTermModelMockLocationB.getFirstObject()!!.term
        val expectedId = collectionsTermModelMockLocationB.getFirstObject()!!.priRef
        collectionsService.createPublisherPlace("Oslo")
            .test()
            .expectNext(PublisherPlace(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createPublisherPlace should throw BadRequestBodyException if publisher place is empty`() {
        assertThrows<BadRequestBodyException> { collectionsService.createPublisherPlace("") }
    }

    @Test
    fun `createLanguage should return RecordAlreadyExistsException if searchLanguage returns non-empty list`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        collectionsService.createLanguage("nob")
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Language 'nob' already exists" }
            .verify()
    }

    @Test
    fun `createLanguage should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA)
        every { collectionsRepository.createTermRecord(any(), any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        val expectedTerm = collectionsTermModelMockLanguageA.getFirstObject()!!.term
        val expectedId = collectionsTermModelMockLanguageA.getFirstObject()!!.priRef
        collectionsService.createLanguage("nob")
            .test()
            .expectNext(Language(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createLanguage should throw BadRequestBodyException if language code is not a valid ISO-639-2 language code`() {
        assertThrows<BadRequestBodyException> { collectionsService.createLanguage("") }
        assertThrows<BadRequestBodyException> { collectionsService.createLanguage("en") }
        assertThrows<BadRequestBodyException> { collectionsService.createLanguage("english") }
    }

    @Test
    fun `createNewspaperItem should return correctly mapped item record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        collectionsService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null), it) }
            .verifyComplete()
    }

    @Test
    fun `createNewspaperItem should correctly encode the item object sent to json string`() {
        every { collectionsRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.createTextsRecord(yearWorkEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        collectionsService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null), it) }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(itemEncodedDto) }
    }

    @Test
    fun `createNewspaperItem should throw CollectionsItemNotFound if item could not be found`() {
        every { collectionsRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelEmptyRecordListMock)
        every { collectionsRepository.createTextsRecord(yearWorkEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        collectionsService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsItemNotFound &&
                it.message!!.contains("New item not found")
            }
            .verify()
    }

    @Test
    fun `createNewspaperItem should ignore URN if is is a physical item`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        collectionsService.createNewspaperItem(newspaperInputDtoItemMockB.copy(digital = false))
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(itemEncodedDtoPhysical) }
    }

    @Test
    fun `createManifestation should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        collectionsService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsManifestationNotFound &&
                it.message!!.contains("New manifestation not found")
            }
            .verify()
    }

    @Test
    fun `createManifestation should return correctly mapped manifestation record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        collectionsService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject()!!, it)
            }
            .verifyComplete()
    }

    @Test
    fun `createManifestation should correctly encode the manifestation object sent to json string`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        val encodedValue = Json.encodeToString(
            ManifestationDto(
                partOfReference = "1",
                recordType = CollectionsRecordType.MANIFESTATION.value,
                dateStart = LocalDate.now().toString(),
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        collectionsService.createManifestation("1", LocalDate.now())
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject()!!, it)
            }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(encodedValue) }
    }

    @Test
    fun `createYearWork should throw CollectionsYearWorkNotFound if year work could not be found`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        collectionsService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsYearWorkNotFound && it.message!!.contains("New year not found")
            }
            .verify()
    }

    @Test
    fun `createYearWork should return correctly mapped year work record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockYearWorkA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA)

        collectionsService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockYearWorkA.getFirstObject()!!, it)
            }
            .verifyComplete()
    }

    @Test
    fun `createYearWork should correctly encode the year work object sent to json string`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockYearWorkA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockYearWorkA)

        val encodedValue = Json.encodeToString(
            YearDto(
                partOfReference = "1",
                recordType = CollectionsRecordType.WORK.value,
                descriptionType = CollectionsDescriptionType.YEAR.value,
                dateStart = "2023",
                title = "2023",
                inputName = "Bikube API",
                inputSource = "texts>texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts"
            )
        )

        collectionsService.createYearWork("1", "2023")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockYearWorkA.getFirstObject()!!, it)
            }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(encodedValue) }
    }

    @Test
    fun `searchItemByTitle should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA)
        val expectedMock = collectionsPartsObjectMockItemA.copy().partsReference!!
        collectionsService.getItemsByTitle("1", LocalDate.parse("2020-01-01"), true, MaterialType.NEWSPAPER)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Item(
                        catalogueId = expectedMock.priRef!!,
                        name = expectedMock.getName(),
                        date = expectedMock.getItemDate(),
                        materialType = MaterialType.NEWSPAPER.value,
                        titleCatalogueId = "1",
                        titleName = collectionsModelMockTitleA.adlibJson.recordList?.first()?.getName(),
                        digital = true,
                        urn = null
                    ),
                    it
                )
            }
            .verifyComplete()

        verify { collectionsRepository.getSingleCollectionsModel("1") }
    }

    @Test
    fun `searchItemByTitle should return an empty flux if no items are found`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        collectionsService.getItemsByTitle("19", LocalDate.parse("1999-12-24"), true, MaterialType.NEWSPAPER)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify { collectionsRepository.getSingleCollectionsModel("19") }
    }

    @Test
    fun `searchItemByTitle should return an empty flux if title has no year works on given year`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleB)

        collectionsService.getItemsByTitle("6", LocalDate.parse("2000-01-01"), true, MaterialType.NEWSPAPER)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify { collectionsRepository.getSingleCollectionsModel("6") }
    }

    @Test
    fun `searchItemByTitle should return an empty flux if year work has no manifestations`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleC)

        collectionsService.getItemsByTitle("7", LocalDate.parse("2000-01-01"), true, MaterialType.NEWSPAPER)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify { collectionsRepository.getSingleCollectionsModel("7") }
    }

    @Test
    fun `searchItemByTitle should return an empty flux if manifestation has no items`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleD)

        collectionsService.getItemsByTitle("8", LocalDate.parse("2000-01-01"), true, MaterialType.NEWSPAPER)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }
}
