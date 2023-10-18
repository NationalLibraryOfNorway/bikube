package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleC
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockYearWorkA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockYearWorkB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsPartOfObjectMockSerialWorkA
import no.nb.bikube.core.DtoMock
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.collections.*
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Duration
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ItemControllerIntegrationTest (
    @Autowired private var webClient: WebTestClient
){
    @MockkBean
    private lateinit var axiellRepository: AxiellRepository

    private val titleId = "1"
    private val yearWorkId = "2"
    private val manifestationId = "3"
    private val itemId = "4"

    private fun createItem(item: Item): ResponseSpec {
        return webClient
            .post()
            .uri("/newspapers/items/")
            .bodyValue(item)
            .exchange()
    }

    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun beforeEach() {
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(5)).build()

        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { axiellRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleA.copy())
        every { axiellRepository.getSingleCollectionsModel(yearWorkId) } returns Mono.just(collectionsModelMockYearWorkA.copy())
        every { axiellRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { axiellRepository.getSingleCollectionsModel(itemId) } returns Mono.just(collectionsModelMockItemA.copy())

        val encodedBody = slot<String>()
        every { axiellRepository.createTextsRecord(capture(encodedBody)) } answers {
            val dto = json.decodeFromString<DtoMock>(encodedBody.captured)
            when (dto.recordType) {
                AxiellRecordType.ITEM.value -> Mono.just(collectionsModelMockItemA)
                AxiellRecordType.MANIFESTATION.value -> Mono.just(collectionsModelMockManifestationA)
                AxiellRecordType.WORK.value -> {
                    if (dto.descriptionType == AxiellDescriptionType.SERIAL.value) {
                        Mono.just(collectionsModelMockTitleA)
                    } else {
                        Mono.just(collectionsModelMockYearWorkB)
                    }
                }
                else -> Mono.just(collectionsModelEmptyRecordListMock)
            }
        }
    }

    // TODO: Swap to 201 Created in TT-1184
    @Test
    fun `post-newspapers-items endpoint should return 201 Created with item`() {
        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isOk
            .expectBody<Item>()
    }

    @Test
    fun `post-newspapers-items endpoint should return correctly mapped item`() {
        val testReturn = collectionsModelMockItemA.getFirstObject()!!

        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isOk
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(Item(
                catalogueId = testReturn.priRef,
                name = testReturn.getName(),
                date = testReturn.getItemDate(),
                materialType = "Avis",
                titleCatalogueId = testReturn.getTitleCatalogueId(),
                titleName = testReturn.getTitleName(),
                digital = testReturn.getFormat() == AxiellFormat.DIGITAL,
                urn = testReturn.getUrn()
            ))
            .verifyComplete()
    }

    @Test
    fun `post-newspapers-items endpoint should return 400 bad request if title is missing`() {
        createItem(newspaperItemMockCValidForCreation.copy(titleCatalogueId = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-items endpoint should return 400 bad request if date is missing`() {
        createItem(newspaperItemMockCValidForCreation.copy(date = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-items endpoint should return 400 bad request if digital is missing`() {
        createItem(newspaperItemMockCValidForCreation.copy(digital = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-items endpoint should return 400 bad request if digital is true and urn is missing`() {
        createItem(newspaperItemMockCValidForCreation.copy(digital = true, urn = null))
            .expectStatus().isBadRequest
    }

    // TODO: uncomment in TT-1184
    //    @Test
    //    fun `post-newspapers-items endpoint should ignore urn for physical items`() {
    //        createItem(newspaperItemMockCValidForCreation.copy(digital = false, urn = "digitest_123_blablabla"))
    //            .expectStatus().isOk
    //            .returnResult<Item>()
    //            .responseBody
    //            .test()
    //            .expectNextMatches { it.urn == null }
    //            .verifyComplete()
    //    }

    @Test
    fun `post-newspapers-items endpoint should return 404 not found if title ID is not a title`() {
        createItem(newspaperItemMockCValidForCreation.copy(titleCatalogueId = itemId))
            .expectStatus().isNotFound
    }

    @Test
    fun `post-newspapers-items endpoint should return 404 not found if title ID is not found`() {
        createItem(newspaperItemMockCValidForCreation.copy(titleCatalogueId = "82822828"))
            .expectStatus().isNotFound
    }


    @Test
    fun `post-newspapers-items endpoint should use year work and manifestation if it exists`() {
        createItem(newspaperItemMockCValidForCreation.copy())
            .expectStatus().isOk

        verify(exactly = 1) { axiellRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should create year work and manifestation if year work is not found`() {
        every { axiellRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleB.copy())
        every { axiellRepository.getSingleCollectionsModel(yearWorkId) } returns Mono.just(collectionsModelMockYearWorkA.copy())

        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isOk

        verify(exactly = 3) { axiellRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should create correct manifestation if not found`() {
        every { axiellRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleC.copy())

        createItem(newspaperItemMockCValidForCreation.copy(date = LocalDate.parse("2000-01-01")))
            .expectStatus().isOk

        verify(exactly = 2) { axiellRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should link item to manifestation and upward`() {
        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isOk
            .returnResult<Item>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(collectionsPartOfObjectMockSerialWorkA.partOfReference!!.priRef, it.titleCatalogueId)
            }
            .verifyComplete()
    }
}
