package no.nb.bikube.api.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.api.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.api.catalogue.collections.exception.CollectionsException
import no.nb.bikube.api.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsPhysicalItemMissingContainer
import no.nb.bikube.api.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.model.dto.*
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.api.catalogue.collections.service.CollectionsLocationService
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.exception.NotSupportedException
import no.nb.bikube.api.core.exception.RecordAlreadyExistsException
import no.nb.bikube.api.core.model.*
import no.nb.bikube.api.core.util.DateUtils
import no.nb.bikube.api.newspaper.NewspaperMockData
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.toString

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
        partOfReference = NewspaperMockData.newspaperItemMockB.catalogueId,
        recordType = CollectionsRecordType.MANIFESTATION.value,
        date = NewspaperMockData.newspaperItemMockB.date.toString(),
        inputName = CollectionsModelMockData.TEST_USERNAME,
        inputNotes = CollectionsModelMockData.INPUT_NOTES,
        inputSource = CollectionsDatabase.NEWSPAPER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = CollectionsDatabase.NEWSPAPER.value
    ))

    private val itemEncodedDto = Json.encodeToString(ItemDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        format = CollectionsFormat.DIGITAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        alternativeNumberList = listOf(NewspaperMockData.urnMock),
        inputName = CollectionsModelMockData.TEST_USERNAME,
        inputNotes = CollectionsModelMockData.INPUT_NOTES,
        inputSource = CollectionsDatabase.NEWSPAPER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = CollectionsDatabase.NEWSPAPER.value,
        partOfReference = NewspaperMockData.newspaperItemMockB.catalogueId,
        urn = NewspaperMockData.urnMock.name
    ))

    private val itemEncodedDtoPhysical = Json.encodeToString(ItemDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        format = CollectionsFormat.PHYSICAL.value,
        recordType = CollectionsRecordType.ITEM.value,
        alternativeNumberList = null,
        inputName = CollectionsModelMockData.TEST_USERNAME,
        inputNotes = CollectionsModelMockData.INPUT_NOTES,
        inputSource = CollectionsDatabase.NEWSPAPER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = CollectionsDatabase.NEWSPAPER.value,
        partOfReference = NewspaperMockData.newspaperItemMockB.catalogueId,
        currentLocationName = CollectionsModelMockData.collectionsLocationObjectMock.priRef
    ))

    private val titleEncodedDto = Json.encodeToString(TitleDto(
        priRef = "1600000000",
        objectNumber = "TE-1600000000",
        titles = listOf(CollectionsTitleDto(NewspaperMockData.newspaperTitleMockB.name!!, "Originaltittel")),
        dateStart = NewspaperMockData.newspaperTitleMockB.startDate.toString(),
        dateEnd = NewspaperMockData.newspaperTitleMockB.endDate.toString(),
        publisher = NewspaperMockData.newspaperTitleMockB.publisher,
        placeOfPublication = NewspaperMockData.newspaperTitleMockB.publisherPlace,
        language = NewspaperMockData.newspaperTitleMockB.language,
        recordType = CollectionsRecordType.WORK.value,
        medium = "Tekst",
        subMedium = "Aviser",
        inputName = CollectionsModelMockData.TEST_USERNAME,
        inputNotes = CollectionsModelMockData.INPUT_NOTES,
        inputSource = CollectionsDatabase.NEWSPAPER.value,
        inputDate = LocalDate.now().toString(),
        inputTime = mockedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = CollectionsDatabase.NEWSPAPER.value)
    )

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleC
        )

        val body = NewspaperMockData.newspaperTitleInputDtoMockB.copy()

        newspaperService.createNewspaperTitle(body)
            .test()
            .expectNextMatches { it == NewspaperMockData.newspaperTitleMockB }
            .verifyComplete()

        verify { collectionsRepository.createNewspaperRecord(titleEncodedDto) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.error(CollectionsException("Error creating title"))

        newspaperService.createNewspaperTitle(NewspaperMockData.newspaperTitleInputDtoMockB)
            .test()
            .expectErrorMatches { it is CollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getSingleItem should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemA.copy())
        val testRecord = CollectionsModelMockData.collectionsModelMockItemA.getFirstObject()
        val testSerialWork = CollectionsModelMockData.collectionsPartOfObjectMockSerialWorkA.partOfReference!!

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationA.copy())

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if object is a work`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleA.copy())

        newspaperService.getSingleItem("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleItem should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelEmptyRecordListMock.copy())

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
                    CollectionsModelMockData.collectionsModelMockItemA.getFirstObject().copy(),
                    CollectionsModelMockData.collectionsModelMockItemA.getFirstObject().copy()
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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleA.copy())
        val testRecord = CollectionsModelMockData.collectionsModelMockTitleA.getFirstObject()

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if object is an item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `getSingleTitle should throw CollectionsTitleNotFound if no items are received`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("1") } returns Mono.just(
            CollectionsModelMockData.collectionsModelEmptyRecordListMock.copy())

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
                    CollectionsModelMockData.collectionsModelMockTitleA.getFirstObject().copy(),
                    CollectionsModelMockData.collectionsModelMockTitleB.getFirstObject().copy()
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
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleC
        )

        newspaperService.createNewspaperTitle(NewspaperMockData.newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(NewspaperMockData.newspaperTitleMockB, it) }
            .verifyComplete()
    }

    @Test
    fun `createTitle should correctly encode the title object sent to json string`() {
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockTitleC)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleC
        )

        newspaperService.createNewspaperTitle(NewspaperMockData.newspaperTitleInputDtoMockB.copy())
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(NewspaperMockData.newspaperTitleMockB, it) }
            .verifyComplete()

        verify { collectionsRepository.createNewspaperRecord(titleEncodedDto) }
    }

    @Test
    fun `createPublisher should return RecordAlreadyExistsException if searchPublisher returns non-empty list`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(CollectionsModelMockData.collectionsNameModelMockA)
        newspaperService.createPublisher("1", CollectionsModelMockData.TEST_USERNAME)
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Publisher '1' already exists" }
            .verify()
    }

    @Test
    fun `createPublisher should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(CollectionsModelMockData.collectionsNameModelWithEmptyRecordListA)
        every { collectionsRepository.createNameRecord(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsNameModelMockA)
        val expectedName = CollectionsModelMockData.collectionsNameModelMockA.getFirstObject().name
        val expectedId = CollectionsModelMockData.collectionsNameModelMockA.getFirstObject().priRef
        newspaperService.createPublisher("Schibsted", CollectionsModelMockData.TEST_USERNAME)
            .test()
            .expectNext(Publisher(expectedName, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createNameRecord(any(), any()) }
    }

    @Test
    fun `createPublisher should throw BadRequestBodyException if publisher is empty`() {
        assertThrows<BadRequestBodyException> { newspaperService.createPublisher("",
            CollectionsModelMockData.TEST_USERNAME
        ) }
    }

    @Test
    fun `createPublisherPlace should return RecordAlreadyExistsException if searchPublisherPlace returns non-empty list`() {
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelMockLocationB)
        val publisherPlaceName = CollectionsModelMockData.collectionsTermModelMockLocationB.getFirstObject().term
        newspaperService.createPublisherPlace(publisherPlaceName, CollectionsModelMockData.TEST_USERNAME)
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
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelWithEmptyRecordListA)
        every { collectionsRepository.createTermRecord(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelMockLocationB)
        val expectedTerm = CollectionsModelMockData.collectionsTermModelMockLocationB.getFirstObject().term
        val expectedId = CollectionsModelMockData.collectionsTermModelMockLocationB.getFirstObject().priRef
        newspaperService.createPublisherPlace("Oslo", CollectionsModelMockData.TEST_USERNAME)
            .test()
            .expectNext(PublisherPlace(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createPublisherPlace should throw BadRequestBodyException if publisher place is empty`() {
        assertThrows<BadRequestBodyException> { newspaperService.createPublisherPlace("",
            CollectionsModelMockData.TEST_USERNAME
        ) }
    }

    @Test
    fun `createLanguage should return RecordAlreadyExistsException if searchLanguage returns non-empty list`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelMockLanguageA)
        newspaperService.createLanguage("nob", CollectionsModelMockData.TEST_USERNAME)
            .test()
            .expectSubscription()
            .expectErrorMatches { it is RecordAlreadyExistsException && it.message == "Language 'nob' already exists" }
            .verify()
    }

    @Test
    fun `createLanguage should call createRecord if search returns empty recordList`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelWithEmptyRecordListA)
        every { collectionsRepository.createTermRecord(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsTermModelMockLanguageA)
        val expectedTerm = CollectionsModelMockData.collectionsTermModelMockLanguageA.getFirstObject().term
        val expectedId = CollectionsModelMockData.collectionsTermModelMockLanguageA.getFirstObject().priRef
        newspaperService.createLanguage("nob", CollectionsModelMockData.TEST_USERNAME)
            .test()
            .expectNext(Language(expectedTerm, expectedId))
            .verifyComplete()

        verify { collectionsRepository.createTermRecord(any(), any()) }
    }

    @Test
    fun `createLanguage should throw BadRequestBodyException if language code is not a valid ISO-639-2 language code`() {
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("",
            CollectionsModelMockData.TEST_USERNAME
        ) }
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("en",
            CollectionsModelMockData.TEST_USERNAME
        ) }
        assertThrows<BadRequestBodyException> { newspaperService.createLanguage("english",
            CollectionsModelMockData.TEST_USERNAME
        ) }
    }

    @Test
    fun `createNewspaperItem should return correctly mapped item record`() {
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        newspaperService.createNewspaperItem(NewspaperMockData.newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(NewspaperMockData.newspaperItemMockB.copy(titleCatalogueId = null, date = null), it) }
            .verifyComplete()
    }

    @Test
    fun `createNewspaperItem should correctly encode the item object sent to json string`() {
        every { collectionsRepository.createNewspaperRecord(itemEncodedDto) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.createNewspaperRecord(manifestationEncodedDto) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        newspaperService.createNewspaperItem(NewspaperMockData.newspaperInputDtoItemMockB)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(NewspaperMockData.newspaperItemMockB.copy(titleCatalogueId = null, date = null), it) }
            .verifyComplete()

        verify { collectionsRepository.createNewspaperRecord(itemEncodedDto) }
    }

    @Test
    fun `createNewspaperItem should throw CollectionsItemNotFound if item could not be found`() {
        every { collectionsRepository.createNewspaperRecord(itemEncodedDto) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)
        every { collectionsRepository.createNewspaperRecord(manifestationEncodedDto) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )

        newspaperService.createNewspaperItem(NewspaperMockData.newspaperInputDtoItemMockB)
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
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionLocationService.createContainerIfNotExists(any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsLocationObjectMock
        )

        newspaperService.createNewspaperItem(
            NewspaperMockData.newspaperInputDtoItemMockB.copy(digital = false, containerId = CollectionsModelMockData.collectionsLocationObjectMock.priRef)
        )
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify { collectionsRepository.createNewspaperRecord(itemEncodedDtoPhysical) }
    }

    @Test
    fun `createNewspaperItem should get or create container if item is physical and has container ID`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionLocationService.createContainerIfNotExists(any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsLocationObjectMock
        )
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        // Given that the item is physical and has a container ID
        val barcode = "123"
        val testItem = NewspaperMockData.newspaperInputDtoItemMockB.copy(digital = false, containerId = barcode)

        // When creating the item
        newspaperService.createNewspaperItem(testItem)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        // Then the container should be created
        verify (exactly = 1) { collectionLocationService.createContainerIfNotExists(barcode,
            CollectionsModelMockData.TEST_USERNAME
        ) }
    }

    @Test
    fun `createNewspaperItem should not get or create container if item is digital`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        // Given that the item is digital
        val testItem = NewspaperMockData.newspaperInputDtoItemMockB.copy(digital = true, containerId = "123")

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        // Given that the item is physical and does not have a container ID
        val testItem = NewspaperMockData.newspaperInputDtoItemMockB.copy(digital = false, containerId = null)

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
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.createManifestation("1", LocalDate.now(),
            CollectionsModelMockData.TEST_USERNAME,
            CollectionsModelMockData.TEST_NOTES,
            CollectionsModelMockData.TEST_NUMBER
        )
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
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)

        newspaperService.createManifestation("1", LocalDate.now(),
            CollectionsModelMockData.TEST_USERNAME,
            CollectionsModelMockData.TEST_NOTES,
            CollectionsModelMockData.TEST_NUMBER
        )
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(CollectionsModelMockData.collectionsModelMockManifestationA.getFirstObject(), it)
            }
            .verifyComplete()
    }

    @Test
    fun `createManifestation should correctly encode the manifestation object sent to json string`() {
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)

        val encodedValue = Json.encodeToString(
            ManifestationDto(
                priRef = "1600000000",
                objectNumber = "TE-1600000000",
                partOfReference = "1",
                recordType = CollectionsRecordType.MANIFESTATION.value,
                date = LocalDate.now().toString(),
                edition = CollectionsModelMockData.TEST_NUMBER,
                inputName = CollectionsModelMockData.TEST_USERNAME,
                inputNotes = CollectionsModelMockData.INPUT_NOTES,
                inputSource = CollectionsDatabase.NEWSPAPER.value,
                inputDate = LocalDate.now().toString(),
                inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
                dataset = CollectionsDatabase.NEWSPAPER.value,
                notes = CollectionsModelMockData.TEST_NOTES,
                alternativeNumbers = listOf(AlternativeNumberInput(CollectionsModelMockData.TEST_NUMBER, "Nummer"))
            )
        )

        newspaperService.createManifestation("1", LocalDate.now(),
            CollectionsModelMockData.TEST_USERNAME,
            CollectionsModelMockData.TEST_NOTES,
            CollectionsModelMockData.TEST_NUMBER
        )
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(CollectionsModelMockData.collectionsModelMockManifestationA.getFirstObject(), it)
            }
            .verifyComplete()

        verify { collectionsRepository.createNewspaperRecord(encodedValue) }
    }

    @Test
    fun `getItemsByTitleAndDate should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationC
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationC)
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationC)

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
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

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
        every { collectionsRepository.getManifestations(any(), any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockTitleA)
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

        newspaperService.getItemsByTitleAndDate("8", LocalDate.parse("2000-01-01"), true)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `createMissingItem should return correctly mapped item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

        newspaperService.createMissingItem(NewspaperMockData.missingItemDtoMock)
            .test()
            .expectSubscription()
            .assertNext { Assertions.assertEquals(NewspaperMockData.newspaperItemMockDNoItem.copy(date = null, titleCatalogueId = "22"), it) }
            .verifyComplete()
    }

    @Test
    fun `createMissingItem should create manifestation if it does not exist`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelEmptyRecordListMock
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemB)

        newspaperService.createMissingItem(NewspaperMockData.missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `createMissingItem should return manifestation if it exists`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemB
        )
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationB
        )
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

        newspaperService.createMissingItem(NewspaperMockData.missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 0) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `createMissingItem should return error if title does not exist`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelEmptyRecordListMock
        )

        newspaperService.createMissingItem(NewspaperMockData.missingItemDtoMock)
            .test()
            .expectSubscription()
            .expectError()
            .verify()
    }

    @Test
    fun `updatePhysicalNewspaper should update when manifestation ID is given`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationA
        )
        every { collectionsRepository.updateNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 1){ collectionsRepository.updateNewspaperRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should encode body correctly`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockManifestationA
        )
        every { collectionsRepository.updateNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        val dto = CollectionsObjectUpdateDto(
            priRef = NewspaperMockData.newspaperItemUpdateDtoMockA.manifestationId,
            notes = CollectionsModelMockData.TEST_NOTES,
            alternativeNumbers = listOf(AlternativeNumberInput(name = NewspaperMockData.newspaperItemUpdateDtoMockA.number!!, type = "Nummer")),
            editName = CollectionsModelMockData.TEST_USERNAME,
            editTime = LocalTime.now().toString(),
            editDate = DateUtils.createDateString(LocalDate.now())
        )

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 1){ collectionsRepository.updateNewspaperRecord(Json.encodeToString(dto)) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelEmptyRecordListMock
        )

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(CollectionsManifestationNotFound::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateNewspaperRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if id belongs to item`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockItemA
        )

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateNewspaperRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if id belongs to title`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleA
        )

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()

        verify (exactly = 1){ collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) }
        verify (exactly = 0){ collectionsRepository.updateNewspaperRecord(any()) }
    }

    @Test
    fun `updatePhysicalNewspaper should throw NotSupportedException if update fails due to wrong type`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.collectionsModelMockTitleA
        )
        every { collectionsRepository.updateNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.updatePhysicalNewspaper(NewspaperMockData.newspaperItemUpdateDtoMockA)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should return when valid deletion`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsManifestationNotFound if manifestation could not be found`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(CollectionsManifestationNotFound::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw NotSupportedException if id belongs to item`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockItemA)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw NotSupportedException if id belongs to title`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockTitleA)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(NotSupportedException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should delete manifestation if manifestation does not have items and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1){ collectionsRepository.deleteNewspaperRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsException if manifestation does not have items and deleteManifestation=false`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationB)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", false)
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should throw CollectionsException if manifestation has multiple physical items`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationD)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectError(CollectionsException::class.java)
            .verify()
    }

    @Test
    fun `deletePhysicalItemByManifestationId should delete manifestation when there is only one item, which is physical, and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationE)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 2) { collectionsRepository.deleteNewspaperRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should not delete manifestation when there are only one physical item but deleteManifestation=false`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationE)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", false)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.deleteNewspaperRecord(any()) }
    }

    @Test
    fun `deletePhysicalItemByManifestationId should not delete manifestation when there are more items than one physical and deleteManifestation=true`() {
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelMockManifestationA)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.collectionsModelEmptyRecordListMock)

        newspaperService.deletePhysicalItemByManifestationId("1", true)
            .test()
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete()

        verify (exactly = 1) { collectionsRepository.deleteNewspaperRecord(any()) }
    }
}
