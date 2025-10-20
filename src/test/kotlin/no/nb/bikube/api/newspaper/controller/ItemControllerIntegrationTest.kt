package no.nb.bikube.api.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import no.nb.bikube.api.catalogue.collections.DtoMock
import no.nb.bikube.api.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.api.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.model.inputDto.ItemInputDto
import no.nb.bikube.api.newspaper.NewspaperMockData
import no.nb.bikube.api.newspaper.service.UniqueIdService
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

    private val titleId = CollectionsModelMockData.Companion.collectionsModelMockTitleA.getFirstId()!!
    private val manifestationId = CollectionsModelMockData.Companion.collectionsModelMockManifestationC.getFirstId()!!
    private val manifestationId2 = CollectionsModelMockData.Companion.collectionsModelMockManifestationA.getFirstId()!!
    private val itemId = CollectionsModelMockData.Companion.collectionsModelMockItemA.getFirstId()!!

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

        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleA.copy())
        every { collectionsRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getSingleCollectionsModel(manifestationId2) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationB.copy())
        every { collectionsRepository.getSingleCollectionsModel(itemId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockItemA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(titleId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockTitleA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(manifestationId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(itemId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockItemA.copy())
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationA
        )
        every { collectionsRepository.updateNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockManifestationB)
        every { collectionsRepository.deleteNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock)
        every { uniqueIdService.getUniqueId() } returns itemId

        val encodedBody = slot<String>()
        every { collectionsRepository.createNewspaperRecord(capture(encodedBody)) } answers {
            val dto = json.decodeFromString<DtoMock>(encodedBody.captured)
            when (dto.recordType) {
                CollectionsRecordType.ITEM.value -> Mono.just(CollectionsModelMockData.Companion.collectionsModelMockItemA)
                CollectionsRecordType.MANIFESTATION.value -> Mono.just(CollectionsModelMockData.Companion.collectionsModelMockManifestationC)
                CollectionsRecordType.WORK.value -> Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleA)
                else -> Mono.just(CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock)
            }
        }
    }

    @Test
    fun `post-newspapers-items endpoint should return 201 Created with item`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation)
            .expectStatus().isCreated
            .expectBody<Item>()
    }

    @Test
    fun `post-newspapers-items endpoint should return correctly mapped item`() {
        val testReturn = CollectionsModelMockData.Companion.collectionsModelMockItemA.getFirstObject()

        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation)
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
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(digital = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-items endpoint should return 400 bad request if digital is true and urn is missing`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(digital = true, urn = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-items endpoint should return 404 not found if title ID is not a title`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(titleCatalogueId = itemId))
            .expectStatus().isNotFound
    }

    @Test
    fun `post-newspapers-items endpoint should return 404 not found if title ID is not found`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(titleCatalogueId = "82822828"))
            .expectStatus().isNotFound
    }


    @Test
    fun `post-newspapers-items endpoint should use manifestation if it exists`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy())
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `post-newspapers-items endpoint should create correct manifestation if not found`() {
        val item = NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(date = LocalDate.parse("2000-01-01"))

        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleB.copy())
        every { collectionsRepository.getSingleCollectionsModel(item.titleCatalogueId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockTitleB.copy())
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockManifestationE.copy())
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
        )

        createItem(item)
            .expectStatus().isCreated

        verify(exactly = 2) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `post-newspaper-items endpoint should return 409 conflict if manifestation already has item with given format`() {
        val item = NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(date = LocalDate.parse("2000-01-01"))

        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleB.copy())
        every { collectionsRepository.getSingleCollectionsModel(item.titleCatalogueId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockTitleB.copy())
        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationA.copy())

        createItem(item)
            .expectStatus().isEqualTo(409)
    }

    @Test
    fun `post-newspapers-items endpoint should link item to manifestation and title`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation)
            .expectStatus().isCreated
            .returnResult<Item>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(CollectionsModelMockData.Companion.collectionsPartOfObjectMockSerialWorkA.partOfReference!!.priRef, it.titleCatalogueId)
            }
            .verifyComplete()
    }

    @Test
    fun `post-newspapers-items endpoint should give 400 bad request if item status is provided for a physical item`() {
        createItem(NewspaperMockData.Companion.newspaperItemMockCValidForCreation.copy(digital = false))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post missing-item should return 201 Created with item`() {
        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(NewspaperMockData.Companion.missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated
            .expectBody<Item>()
    }

    @Test
    fun `post missing-item should return correctly mapped item`() {
        val testReturn = CollectionsModelMockData.Companion.collectionsModelMockManifestationB.getFirstObject()

        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(NewspaperMockData.Companion.missingItemDtoMock)
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
            .bodyValue(NewspaperMockData.Companion.missingItemDtoMock.copy(titleCatalogueId = "489653148"))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `post missing-item should use manifestation if it exists`() {
        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(NewspaperMockData.Companion.missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `post missing-item create correct manifestation if not found`() {
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleB.copy())
        every { collectionsRepository.getManifestations(any(), any(), any()) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
        )

        webClient
            .post()
            .uri("/newspapers/items/missing")
            .bodyValue(NewspaperMockData.Companion.missingItemDtoMock)
            .exchange()
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.createNewspaperRecord(any()) }
    }

    @Test
    fun `put item should return 204 No Content on success`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
            .exchange()
            .expectStatus().isNoContent
    }

    @Test
    fun `put item should update values`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
            .exchange()

        verify(exactly = 1) { collectionsRepository.updateNewspaperRecord(withArg {
            Assertions.assertTrue(it.contains(manifestationId))
            Assertions.assertTrue(it.contains("edit.date"))
            Assertions.assertTrue(it.contains("edit.time"))
            Assertions.assertTrue(it.contains("edit.name"))
            Assertions.assertTrue(it.contains(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.username))
            Assertions.assertTrue(it.contains("notes"))
            Assertions.assertTrue(it.contains(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.notes.toString()))
            Assertions.assertTrue(it.contains("Alternative_number"))
            Assertions.assertTrue(it.contains(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.number.toString()))
        })}
    }

    @Test
    fun `put item should not update null-values`() {
        val dto = NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(
            manifestationId = manifestationId,
            number = null
        )

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(dto)
            .exchange()

        verify(exactly = 1) { collectionsRepository.updateNewspaperRecord(withArg {
            Assertions.assertTrue(it.contains(manifestationId))
            Assertions.assertTrue(it.contains("edit.date"))
            Assertions.assertTrue(it.contains("edit.time"))
            Assertions.assertTrue(it.contains("edit.name"))
            Assertions.assertTrue(it.contains(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.username))
            Assertions.assertTrue(it.contains("notes"))
            Assertions.assertTrue(it.contains(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.notes.toString()))

            Assertions.assertFalse(it.contains("Alternative_number"))
        })}
    }

    @Test
    fun `put item should return 404 NOT FOUND when id is not found`() {
        val dummyId = "123123123"
        every { collectionsRepository.getSingleCollectionsModel(dummyId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock.copy())

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = dummyId))
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `put item should return 400 BAD REQUEST when id is not manifestation`() {
        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = itemId))
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = titleId))
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `put item should return 500 Server Error if the catalog returns an error`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(manifestationId) } returns Mono.just(
            CollectionsModelMockData.Companion.erroneousCollectionsModelMock
        )

        webClient
            .put()
            .uri("/newspapers/items")
            .bodyValue(NewspaperMockData.Companion.newspaperItemUpdateDtoMockA.copy(manifestationId = manifestationId))
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
        every { collectionsRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockManifestationD.copy())

        webClient
            .delete()
            .uri("/newspapers/items/physical/$manifestationId")
            .exchange()
            .expectStatus().is5xxServerError
    }
}
