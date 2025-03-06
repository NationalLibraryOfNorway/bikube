package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.INPUT_NOTES
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.TEST_NOTES
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.TEST_NUMBER
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.TEST_USERNAME
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsLocationObjectMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationC
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationD
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationE
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleC
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsNameModelMockA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsNameModelWithEmptyRecordListA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartOfObjectMockSerialWorkA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.catalogue.collections.exception.CollectionsException
import no.nb.bikube.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.catalogue.collections.exception.CollectionsPhysicalItemMissingContainer
import no.nb.bikube.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.catalogue.collections.model.dto.*
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.catalogue.collections.service.CollectionsLocationService
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.exception.RecordAlreadyExistsException
import no.nb.bikube.core.model.*
import no.nb.bikube.core.util.DateUtils
import no.nb.bikube.newspaper.NewspaperMockData.Companion.missingItemDtoMock
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperInputDtoItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockDNoItem
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemUpdateDtoMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.urnMock
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
class NewspaperServiceTest {

    companion object {
        @JvmStatic
        @AfterAll
        fun unmockLocalTime() {
            unmockkStatic(LocalTime::class)
        }
    }

    @Autowired
    private lateinit var newspaperService: NewspaperService

    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    @MockkBean
    private lateinit var collectionLocationService: CollectionsLocationService

    @MockkBean
    private lateinit var uniqueIdService: UniqueIdService

    private val mockedTime = LocalTime.of(9, 30, 0)

    @BeforeEach
    fun mockLocalTime() {
        mockkStatic(LocalTime::class)
        every { LocalTime.now() } returns mockedTime
        every { uniqueIdService.getUniqueId() } returns "1600000000"
    }

    private val manifestationEncodedDto = Json.encodeToString(ManifestationDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        partOfReference = newspaperItemMockB.catalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        date = newspaperItemMockB.date.toString(),
        inputName = TEST_USERNAME,
        inputNotes = INPUT_NOTES,
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    ))

    private val itemEncodedDto = Json.encodeToString(ItemDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        format = CollectionsFormat.DIGITAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        alternativeNumberList = listOf(urnMock),
        inputName = TEST_USERNAME,
        inputNotes = INPUT_NOTES,
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId,
        urn = urnMock.name
    ))

    private val itemEncodedDtoPhysical = Json.encodeToString(ItemDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        format = CollectionsFormat.PHYSICAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        alternativeNumberList = null,
        inputName = TEST_USERNAME,
        inputNotes = INPUT_NOTES,
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts",
        partOfReference = newspaperItemMockB.catalogueId,
        currentLocationName = collectionsLocationObjectMock.priRef
    ))

    private val titleEncodedDto = Json.encodeToString(TitleDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        titles = listOf(CollectionsTitleDto(newspaperTitleMockB.name!!, "Originaltittel")),
        dateStart = newspaperTitleMockB.startDate.toString(),
        dateEnd = newspaperTitleMockB.endDate.toString(),
        publisher = newspaperTitleMockB.publisher,
        placeOfPublication = newspaperTitleMockB.publisherPlace,
        language = newspaperTitleMockB.language,
        recordType = CollectionsRecordType.WORK.value,
        medium = "Tekst",
        subMedium = "Aviser",
        inputName = TEST_USERNAME,
        inputNotes = INPUT_NOTES,
        inputSource = "texts",
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts")
    )

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleC)

        val body = newspaperTitleInputDtoMockB.copy()

        newspaperService.createNewspaperTitle(body)
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(titleEncodedDto) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.error(CollectionsException("Error creating title"))

        newspaperService.createNewspaperTitle(newspaperTitleInputDtoMockB)
            .test()
            .expectErrorMatches { it is CollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getSingleItem should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemA.copy())
        val testRecord = collectionsModelMockItemA.getFirstObject()
        val testSerialWork = collectionsPartOfObjectMockSerialWorkA.partOfReference!!

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    Item(
                        catalogueId = testRecord.priRef,
                        name = testRecord.getName(),
                        date = testRecord.getDate(),
                        materialType = testSerialWork.getMaterialType()!!.norwegian,
                        titleCatalogueId = testSerialWork.priRef,
                        titleName = testSerialWork.getName(),
                        digital = true,
                        urn = testRecord.getUrn(),
                        location = testRecord.locationBarcode,
                        parentCatalogueId = testRecord.getParentId()
                    ),
                    it
                )
            }
            .verifyComplete()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if object is a manifestation`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelMockManifestationA.copy())

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if object is a work`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelMockTitleA.copy())

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsException if multiple items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockItemA.getFirstObject().copy(),
                    collectionsModelMockItemA.getFirstObject().copy()
                )
            ))
        )

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should return correctly mapped title`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleA.copy())
        val testRecord = collectionsModelMockTitleA.getFirstObject()

        newspaperService.getSingleTitle("1")
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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelMockManifestationA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if object is an item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelMockItemA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsException if multiple items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModel(adlibJson = CollectionsRecordList(
                recordList = listOf(
                    collectionsModelMockTitleA.getFirstObject().copy(),
                    collectionsModelMockTitleB.getFirstObject().copy()
                )
            ))
        )

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `get link to single title should return correct URL`() {
        Assertions.assertEquals(
            newspaperService.getLinkToSingleTitle("12345").toString(),
            "http://collections.com/link/12345"
        )
    }

    @Test
    fun `createTitle should return correctly mapped record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleC)

        newspaperService.createNewspaperTitle(newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `createTitle should correctly encode the title object sent to json string`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleC)

        newspaperService.createNewspaperTitle(newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperTitleMockB, it) }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(titleEncodedDto) }
    }

    @Test
    fun `createPublisher should return RecordAlreadyExistsException if searchPublisher returns non-empty list`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA)
        newspaperService.createPublisher("1", TEST_USERNAME)
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Publisher '1' already exists" }
            .verify()
    }

    @Test
    fun `createPublisher should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelWithEmptyRecordListA)
        every { collectionsRepository.createNameRecord(any(), any()) } returns Mono.just(collectionsNameModelMockA)
        val expectedName = collectionsNameModelMockA.getFirstObject().name
        val expectedId = collectionsNameModelMockA.getFirstObject().priRef
        newspaperService.createPublisher("Schibsted", TEST_USERNAME)
            .test()
            .expectNext(Publisher(expectedName, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createNameRecord(any(), any()) }
    }

    @Test
    fun `createPublisher should throw BadRequestBodyException if publisher is empty`() {
        assertThrows<BadRequestBodyException> { newspaperService.createPublisher("", TEST_USERNAME) }
    }

    @Test
    fun `createPublisherPlace should return RecordAlreadyExistsException if searchPublisherPlace returns non-empty list`() {
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB)
        val publisherPlaceName = collectionsTermModelMockLocationB.getFirstObject().term
        newspaperService.createPublisherPlace(publisherPlaceName, TEST_USERNAME)
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
        val expectedTerm = collectionsTermModelMockLocationB.getFirstObject().term
        val expectedId = collectionsTermModelMockLocationB.getFirstObject().priRef
        newspaperService.createPublisherPlace("Oslo", TEST_USERNAME)
            .test()
            .expectNext(PublisherPlace(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createPublisherPlace should throw BadRequestBodyException if publisher place is empty`() {
        assertThrows<BadRequestBodyException> { newspaperService.createPublisherPlace("", TEST_USERNAME) }
    }

    @Test
    fun `createLanguage should return RecordAlreadyExistsException if searchLanguage returns non-empty list`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        newspaperService.createLanguage("nob", TEST_USERNAME)
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Language 'nob' already exists" }
            .verify()
    }

    @Test
    fun `createLanguage should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA)
        every { collectionsRepository.createTermRecord(any(), any()) } returns Mono.just(collectionsTermModelMockLanguageA)
        val expectedTerm = collectionsTermModelMockLanguageA.getFirstObject().term
        val expectedId = collectionsTermModelMockLanguageA.getFirstObject().priRef
        newspaperService.createLanguage("nob", TEST_USERNAME)
            .test()
            .expectNext(Language(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createLanguage should throw BadRequestBodyException if language code is not a valid ISO-639-2 language code`() {
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("", TEST_USERNAME) }
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("en", TEST_USERNAME) }
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("english", TEST_USERNAME) }
    }

    @Test
    fun `createNewspaperItem should return correctly mapped item record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        newspaperService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null, date = null), it) }
            .verifyComplete()
    }

    @Test
    fun `createNewspaperItem should correctly encode the item object sent to json string`() {
        every { collectionsRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)

        newspaperService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockB.copy(titleCatalogueId = null, date = null), it) }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(itemEncodedDto) }
    }

    @Test
    fun `createNewspaperItem should throw CollectionsItemNotFound if item could not be found`() {
        every { collectionsRepository.createTextsRecord(itemEncodedDto) } returns Mono.just(collectionsModelEmptyRecordListMock)
        every { collectionsRepository.createTextsRecord(manifestationEncodedDto) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.createNewspaperItem(newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsException &&
                it.message!!.contains("Error creating item")
            }
            .verify()
    }

    @Test
    fun `createNewspaperItem should ignore URN if is is a physical item`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionLocationService.createContainerIfNotExists(any(), any()) } returns Mono.just(collectionsLocationObjectMock)

        newspaperService.createNewspaperItem(
            newspaperInputDtoItemMockB.copy(digital = false, containerId = collectionsLocationObjectMock.priRef)
        )
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(itemEncodedDtoPhysical) }
    }

    @Test
    fun `createNewspaperItem should get or create container if item is physical and has container ID`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionLocationService.createContainerIfNotExists(any(), any()) } returns Mono.just(collectionsLocationObjectMock)
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)

        // Given that the item is physical and has a container ID
        val barcode = "123"
        val testItem = newspaperInputDtoItemMockB.copy(digital = false, containerId = barcode)

        // When creating the item
        newspaperService.createNewspaperItem(testItem)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        // Then the container should be created
        verify (exactly = 1) { collectionLocationService.createContainerIfNotExists(barcode, TEST_USERNAME) }
    }

    @Test
    fun `createNewspaperItem should not get or create container if item is digital`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)

        // Given that the item is digital
        val testItem = newspaperInputDtoItemMockB.copy(digital = true, containerId = "123")

        // When creating the item
        newspaperService.createNewspaperItem(testItem)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        // Then a container should not be created
        verify (exactly = 0) { collectionLocationService.createContainerIfNotExists(any(), any())}
    }

    @Test
    fun `createNewspaperItem should return CollectionsPhysicalItemMissingContainer if item is physical and does not have container ID`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)

        // Given that the item is physical and does not have a container ID
        val testItem = newspaperInputDtoItemMockB.copy(digital = false, containerId = null)

        // When creating the item
        newspaperService.createNewspaperItem(testItem)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsPhysicalItemMissingContainer &&
                it.message!!.contains("Physical item must have a container ID")
            }
            .verify()
    }

    @Test
    fun `createManifestation should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.createManifestation("1", LocalDate.now(), TEST_USERNAME, TEST_NOTES, TEST_NUMBER)
            .test()
            .expectSubscription()
            .expectErrorMatches {
                it is CollectionsException &&
                it.message!!.contains("Error creating manifestation")
            }
            .verify()
    }

    @Test
    fun `createManifestation should return correctly mapped manifestation record`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        newspaperService.createManifestation("1", LocalDate.now(), TEST_USERNAME, TEST_NOTES, TEST_NUMBER)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject(), it)
            }
            .verifyComplete()
    }

    @Test
    fun `createManifestation should correctly encode the manifestation object sent to json string`() {
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)

        val encodedValue = Json.encodeToString(
            ManifestationDto(
                priRef = "1600000000",
                objectNumber = "TE-1600000000",
                partOfReference = "1",
                recordType = CollectionsRecordType.MANIFESTATION.value,
                date = LocalDate.now().toString(),
                edition = TEST_NUMBER,
                inputName = TEST_USERNAME,
                inputNotes = INPUT_NOTES,
                inputSource = "texts",
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = "texts",
                notes = TEST_NOTES,
                alternativeNumbers = listOf(AlternativeNumberInput(TEST_NUMBER, "Nummer"))
            )
        )

        newspaperService.createManifestation("1", LocalDate.now(), TEST_USERNAME, TEST_NOTES, TEST_NUMBER)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsModelMockManifestationA.getFirstObject(), it)
            }
            .verifyComplete()

        verify { collectionsRepository.createTextsRecord(encodedValue) }
    }

    @Test
    fun `getItemsByTitleAndDate should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockManifestationC)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationC)
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(collectionsModelMockManifestationC)

        val date = LocalDate.parse("2020-01-01")
        val expected = Item(
            catalogueId = "32",
            name = "Aftenposten",
            date = date,
            materialType = MaterialType.NEWSPAPER.value,
            titleCatalogueId = "1",
            titleName = "Aftenposten",
            digital = true,
            urn = null,
            parentCatalogueId = null
        )

        newspaperService.getItemsByTitleAndDate("1", date, true)
            .test()
            .expectSubscription()
            .assertNext { actual ->
                Assertions.assertEquals(expected, actual)
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsByTitleAndDate should return an empty flux if no items are found`() {
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.getItemsByTitleAndDate("19", LocalDate.parse("1999-12-24"), true)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify(exactly = 1) { collectionsRepository.getManifestations(LocalDate.parse("1999-12-24"), "19") }
    }

    @Test
    fun `getItemsByTitleAndDate should return an empty flux if no manifestations`() {
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.empty()

        newspaperService.getItemsByTitleAndDate("7", LocalDate.parse("2000-01-01"), true)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()

        verify { collectionsRepository.getSingleCollectionsModel(any()) wasNot Called }
        verify { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) wasNot Called }
    }

    @Test
    fun `getItemsByTitleAndDate should return an empty flux if manifestation has no items`() {
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(collectionsModelMockTitleA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.getItemsByTitleAndDate("8", LocalDate.parse("2000-01-01"), true)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createMissingItem should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.createMissingItem(missingItemDtoMock)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(newspaperItemMockDNoItem.copy(date = null, titleCatalogueId = "22"), it) }
            .verifyComplete()
    }

    @Test
    fun `createMissingItem should create manifestation if it does not exist`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelEmptyRecordListMock)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockItemB)

        newspaperService.createMissingItem(missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `createMissingItem should return manifestation if it exists`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.createMissingItem(missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 0) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `createMissingItem should return error if title does not exist`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.createMissingItem(missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectError()
            .verify()
    }

    @Test
    fun `updatePhysicalNewspaper should update when manifestation ID is given`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.updateTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 1){ collectionsRepository.updateTextsRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should encode body correctly`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.updateTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationB)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        val dto = CollectionsObjectUpdateDto(
            priRef = newspaperItemUpdateDtoMockA.manifestationId,
            notes = TEST_NOTES,
            alternativeNumbers = listOf(AlternativeNumberInput(name = newspaperItemUpdateDtoMockA.number!!, type = "Nummer")),
            editName = TEST_USERNAME,
            editTime = LocalTime.now().toString(),
            editDate = DateUtils.createDateString(LocalDate.now())
        )

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 1){ collectionsRepository.updateTextsRecord(Json.encodeToString(dto)) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(CollectionsManifestationNotFound::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateTextsRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if id belongs to item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockItemA)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateTextsRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if id belongs to title`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleA)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateTextsRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if update fails due to wrong type`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelMockTitleA)
        every { collectionsRepository.updateTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.updatePhysicalNewspaper(newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should return when valid deletion`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(CollectionsManifestationNotFound::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw NotSupportedException if id belongs to item`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockItemA)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw NotSupportedException if id belongs to title`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should delete manifestation if manifestation does not have items and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1){ collectionsRepository.deleteTextsRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsException if manifestation does not have items and deleteManifestation=false`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", false)
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsException if manifestation has multiple physical items`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationD)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should delete manifestation when there is only one item, which is physical, and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationE)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 2) { collectionsRepository.deleteTextsRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should not delete manifestation when there are only one physical item but deleteManifestation=false`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationE)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", false)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.deleteTextsRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should not delete manifestation when there are more items than one physical and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.deleteTextsRecord(any()) }
    }
}
