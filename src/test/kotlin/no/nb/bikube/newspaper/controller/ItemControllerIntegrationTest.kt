package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationC
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationD
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartOfObjectMockSerialWorkA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.erroneousCollectionsModelMock
import no.nb.bikube.catalogue.collections.DtoMock
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.missingItemDtoMock
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemUpdateDtoMockA
import no.nb.bikube.newspaper.service.UniqueIdService
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
class ItemControllerIntegrationTest {
    @Autowired private lateinit var webClient: WebTestClient

    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    @MockkBean
    private lateinit var uniqueIdService: UniqueIdService

    private val titleId = collectionsModelMockTitleA.getFirstId()!!
    private val manifestationId = collectionsModelMockManifestationC.getFirstId()!!
    private val manifestationId2 = collectionsModelMockManifestationA.getFirstId()!!
    private val itemId = collectionsModelMockItemA.getFirstId()!!

    private fun createItem(item: ItemInputDto): ResponseSpec {
        return webClient
            .post()
            .uri("/newspapers/items")
            .bodyValue(item)
            .exchange()
    }

    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun beforeEach() {
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(1000)).build()

        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleA.copy())
        every { collectionsRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getSingleCollectionsModel(manifestationId2) } returns Mono.just(collectionsModelMockManifestationB.copy())
        every { collectionsRepository.getSingleCollectionsModel(itemId) } returns Mono.just(collectionsModelMockItemA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(titleId) } returns Mono.just(collectionsModelMockTitleA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(manifestationId) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(itemId) } returns Mono.just(collectionsModelMockItemA.copy())
        every { collectionsRepository.getManifestationsByDateAndTitle(any(), any()) } returns Mono.just(collectionsModelMockManifestationA)
        every { collectionsRepository.updateTextsRecord(any()) } returns Mono.just(collectionsModelMockManifestationB)
        every { collectionsRepository.deleteTextsRecord(any()) } returns Mono.just(collectionsModelEmptyRecordListMock)
        every { uniqueIdService.getUniqueId() } returns itemId

        val encodedBody = slot<String>()
        every { collectionsRepository.createTextsRecord(capture(encodedBody)) } answers {
            val dto = json.decodeFromString<DtoMock>(encodedBody.captured)
            when (dto.recordType) {
                CollectionsRecordType.ITEM.value -> Mono.just(collectionsModelMockItemA)
                CollectionsRecordType.MANIFESTATION.value -> Mono.just(collectionsModelMockManifestationC)
                CollectionsRecordType.WORK.value -> Mono.just(collectionsModelMockTitleA)
                else -> Mono.just(collectionsModelEmptyRecordListMock)
            }
        }
    }

    @Test
    fun `post-newspapers-items endpoint should return 201 Created with item`() {
        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isCreated
            .expectBody<Item>()
    }

    @Test
    fun `post-newspapers-items endpoint should return correctly mapped item`() {
        val testReturn = collectionsModelMockItemA.getFirstObject()

        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isCreated
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(Item(
                catalogueId = testReturn.priRef,
                name = testReturn.getName(),
                date = testReturn.getDate(),
                materialType = "Avis",
                titleCatalogueId = testReturn.getTitleCatalogueId(),
                titleName = testReturn.getTitleName(),
                digital = testReturn.getFormat() == CollectionsFormat.DIGITAL,
                urn = testReturn.getUrn(),
                location = testReturn.locationBarcode,
                parentCatalogueId = testReturn.getParentId()
            ))
            .verifyComplete()
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
    fun `post-newspapers-items endpoint should use manifestation if it exists`() {
        createItem(newspaperItemMockCValidForCreation.copy())
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should create correct manifestation if not found`() {
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleB.copy())
        every { collectionsRepository.getManifestationsByDateAndTitle(any(), any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        createItem(newspaperItemMockCValidForCreation.copy(date = LocalDate.parse("2000-01-01")))
            .expectStatus().isCreated

        verify(exactly = 2) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should link item to manifestation and title`() {
        createItem(newspaperItemMockCValidForCreation)
            .expectStatus().isCreated
            .returnResult<Item>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(collectionsPartOfObjectMockSerialWorkA.partOfReference!!.priRef, it.titleCatalogueId)
            }
            .verifyComplete()
    }

    @Test
    fun `post missing-item should return 201 Created with item`() {
        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Item>()
    }

    @Test
    fun `post missing-item should return correctly mapped item`() {
        val testReturn = collectionsModelMockManifestationB.getFirstObject()

        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(missingItemDtoMock)
            .exchange()
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(Item(
                catalogueId = testReturn.priRef,
                name = testReturn.getName(),
                date = testReturn.getDate(),
                materialType = "Avis",
                titleCatalogueId = testReturn.getTitleCatalogueId(),
                titleName = testReturn.getTitleName(),
                digital = null,
                urn = null,
                location = null,
                parentCatalogueId = testReturn.getParentId()
            ))
            .verifyComplete()
    }

    @Test
    fun `post missing-item should return 404 not found if title ID is not found`() {
        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(missingItemDtoMock.copy(titleCatalogueId = "489653148"))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `post missing-item should use manifestation if it exists`() {
        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `post missing-item create correct manifestation if not found`() {
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleB.copy())
        every { collectionsRepository.getManifestationsByDateAndTitle(any(), any()) } returns Mono.just(collectionsModelEmptyRecordListMock)

        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.createTextsRecord(any()) }
    }

    @Test
    fun `put item should return 204 No Content on success`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `put item should update values`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
            .exchange()

        verify(exactly = 1) { collectionsRepository.updateTextsRecord(withArg {
            Assertions.assertTrue(it.contains(manifestationId))
            Assertions.assertTrue(it.contains("edit.date"))
            Assertions.assertTrue(it.contains("edit.time"))
            Assertions.assertTrue(it.contains("edit.name"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.username))
            Assertions.assertTrue(it.contains("dating.date.start"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.date.toString()))
            Assertions.assertTrue(it.contains("notes"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.notes.toString()))
            Assertions.assertTrue(it.contains("Alternative_number"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.number.toString()))
        })}
    }

    @Test
    fun `put item should not update null-values`() {
        val dto = newspaperItemUpdateDtoMockA.copy(
            manifestationId = manifestationId,
            date = null,
            number = null
        )

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(dto)
            .exchange()

        verify(exactly = 1) { collectionsRepository.updateTextsRecord(withArg {
            Assertions.assertTrue(it.contains(manifestationId))
            Assertions.assertTrue(it.contains("edit.date"))
            Assertions.assertTrue(it.contains("edit.time"))
            Assertions.assertTrue(it.contains("edit.name"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.username))
            Assertions.assertTrue(it.contains("notes"))
            Assertions.assertTrue(it.contains(newspaperItemUpdateDtoMockA.notes.toString()))

            Assertions.assertFalse(it.contains("dating.date.start"))
            Assertions.assertFalse(it.contains(newspaperItemUpdateDtoMockA.date.toString()))
            Assertions.assertFalse(it.contains("Alternative_number"))
        })}
    }

    @Test
    fun `put item should return 404 NOT FOUND when id is not found`() {
        val dummyId = "123123123"
        every { collectionsRepository.getSingleCollectionsModel(dummyId) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = dummyId))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `put item should return 400 BAD REQUEST when id is not manifestation`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = itemId))
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = titleId))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `put item should return 500 Server Error if the catalog returns an error`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(manifestationId) } returns Mono.just(erroneousCollectionsModelMock)

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
            .exchange()
            .expectStatus().is5xxServerError
    }

    @Test
    fun `delete item should return 204 when deleted`() {
        webClient
            .delete()
            .uri("/newspapers/items/physical/$manifestationId")
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `delete item should return 400 when item is item or title`() {
        webClient
            .delete()
            .uri("/newspapers/items/physical/$itemId")
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .delete()
            .uri("/newspapers/items/physical/$titleId")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `delete item should return 404 when item is not found`() {
        webClient
            .delete()
            .uri("/newspapers/items/physical/123123123")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `delete item should return 500 when manifestation has more than 1 physical item`() {
        every { collectionsRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(collectionsModelMockManifestationD.copy())

        webClient
            .delete()
            .uri("/newspapers/items/physical/$manifestationId")
            .exchange()
            .expectStatus().is5xxServerError
    }
}
