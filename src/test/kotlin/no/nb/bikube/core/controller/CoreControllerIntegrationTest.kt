package no.nb.bikube.core.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockAllTitles
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockYearWorkA
import no.nb.bikube.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.catalogue.collections.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.Title
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CoreControllerIntegrationTest (
    @Autowired private var webClient: WebTestClient
){
    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    private val titleId = "1"
    private val yearWorkId = "2"
    private val manifestationId = "3"
    private val itemId = "4"

    @BeforeEach
    fun beforeEach() {
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(60)).build()

        every { collectionsRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelMockTitleA.copy())
        every { collectionsRepository.getSingleCollectionsModel(yearWorkId) } returns Mono.just(collectionsModelMockYearWorkA.copy())
        every { collectionsRepository.getSingleCollectionsModel(manifestationId) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { collectionsRepository.getSingleCollectionsModel(itemId) } returns Mono.just(collectionsModelMockItemA.copy())
        every { collectionsRepository.getTitleByName(any()) } returns Mono.just(collectionsModelMockAllTitles.copy())
        every { collectionsRepository.getWorkYearForTitle(any(), any()) } returns Mono.just(collectionsModelMockYearWorkA.copy())
    }

    private fun getItem(itemId: String, materialType: MaterialType): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item")
                    .queryParam("catalogueId", itemId)
                    .queryParam("materialType", materialType)
                    .build()
            }
            .exchange()
    }

    private fun getTitle(titleId: String, materialType: MaterialType): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("title")
                    .queryParam("catalogueId", titleId)
                    .queryParam("materialType", materialType)
                    .build()
            }
            .exchange()
    }

    private fun searchTitle(search: String, materialType: MaterialType): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("title", "search")
                    .queryParam("searchTerm", search)
                    .queryParam("materialType", materialType)
                    .build()
            }
            .exchange()
    }

    @Test
    fun `get-item endpoint should return 200 OK with item`() {
        getItem(itemId, MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .expectBody<Item>()
    }

    @Test
    fun `get-item endpoint should return 400 Bad Request for manuscripts`() {
        getItem(itemId, MaterialType.MANUSCRIPT)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-item endpoint should return 400 Bad Request for monographs`() {
        getItem(itemId, MaterialType.MONOGRAPH)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-item endpoint should return 400 Bad Request for periodicals`() {
        getItem(itemId, MaterialType.PERIODICAL)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-item endpoint should return correctly mapped item`() {
        val testItem = collectionsModelMockItemA.getFirstObject()!!

        getItem(itemId, MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(
                Item(
                    catalogueId = testItem.priRef,
                    name = testItem.getName(),
                    date = testItem.getItemDate(),
                    materialType = testItem.getMaterialTypeFromParent()!!.norwegian,
                    titleCatalogueId = testItem.getTitleCatalogueId(),
                    titleName = testItem.getTitleName(),
                    digital = testItem.getFormat() == CollectionsFormat.DIGITAL,
                    urn = testItem.getUrn()
                )
            )
            .verifyComplete()
    }

    @Test
    fun `get-item endpoint should return 404 when item does not exist`() {
        getItem("9903892", MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
    }

    @Test
    fun `get-item endpoint should return 404 with message when ID belongs to title, year work or manifestation`() {
        getItem(titleId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { problemDetail ->
                Assertions.assertEquals("Not Found", problemDetail.title)
                Assertions.assertTrue(problemDetail.detail!!.lowercase().contains("object is not of type item"))
                Assertions.assertNotNull(problemDetail.instance)
            }
            .verifyComplete()

        getItem(yearWorkId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { problemDetail ->
                Assertions.assertEquals("Not Found", problemDetail.title)
                Assertions.assertTrue(problemDetail.detail!!.lowercase().contains("object is not of type item"))
                Assertions.assertNotNull(problemDetail.instance)
            }
            .verifyComplete()

        getItem(manifestationId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { response ->
                Assertions.assertEquals("Not Found", response.title)
                Assertions.assertTrue(response.detail!!.lowercase().contains("object is not of type item"))
                Assertions.assertNotNull(response.instance)
            }
            .verifyComplete()
    }

    @Test
    fun `get-title endpoint should return 200 OK with title`() {
        getTitle(titleId, MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .expectBody<Title>()
    }

    @Test
    fun `get-title endpoint should return 400 bad request for manuscripts`() {
        getTitle(titleId, MaterialType.MANUSCRIPT)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title endpoint should return 400 bad request for monographs`() {
        getTitle(titleId, MaterialType.MONOGRAPH)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title endpoint should return 400 bad request for periodicals`() {
        getTitle(titleId, MaterialType.PERIODICAL)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title endpoint should return correctly mapped title`() {
        val testTitle = collectionsModelMockTitleA.getFirstObject()!!

        getTitle(titleId, MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .returnResult<Title>()
            .responseBody
            .test()
            .expectNext(
                Title(
                    name = testTitle.getName(),
                    startDate = testTitle.getStartDate(),
                    endDate = testTitle.getEndDate(),
                    publisher = testTitle.getPublisher(),
                    publisherPlace = testTitle.getPublisherPlace(),
                    language = testTitle.getLanguage(),
                    materialType = testTitle.getMaterialType()!!.norwegian,
                    catalogueId = testTitle.priRef
                )
            )
            .verifyComplete()
    }

    @Test
    fun `get-title endpoint should return 404 when title does not exist`() {
        getTitle("9903892", MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
    }

    @Test
    fun `get-title endpoint should return 404 with message when ID belongs to year work, manifestation or item`() {
        getTitle(yearWorkId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { problemDetail ->
                Assertions.assertEquals("Not Found", problemDetail.title)
                Assertions.assertTrue(problemDetail.detail!!.lowercase().contains("object is not of type serial"))
                Assertions.assertNotNull(problemDetail.instance)
            }
            .verifyComplete()


        getTitle(itemId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { problemDetail ->
                Assertions.assertEquals("Not Found", problemDetail.title)
                Assertions.assertTrue(problemDetail.detail!!.lowercase().contains("object is not of type work"))
                Assertions.assertNotNull(problemDetail.instance)
            }
            .verifyComplete()

        getTitle(manifestationId, MaterialType.NEWSPAPER)
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext { problemDetail ->
                Assertions.assertEquals("Not Found", problemDetail.title)
                Assertions.assertTrue(problemDetail.detail!!.lowercase().contains("object is not of type work"))
                Assertions.assertNotNull(problemDetail.instance)
            }
            .verifyComplete()
    }

    @Test
    fun `get-title-search endpoint should return 200 OK with all titles`() {
        searchTitle("*", MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNextCount(collectionsModelMockAllTitles.getObjects()!!.size.toLong())
            .verifyComplete()
    }

    @Test
    fun `get-title-search endpoint should return mapped items`() {
        searchTitle("*", MaterialType.NEWSPAPER)
            .returnResult<Title>()
            .responseBody
            .test()
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockAllTitles.getObjects()!![0]))
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockAllTitles.getObjects()!![1]))
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockAllTitles.getObjects()!![2]))
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockAllTitles.getObjects()!![3]))
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockAllTitles.getObjects()!![4]))
            .verifyComplete()
    }

    @Test
    fun `get-title-search endpoint should return 400 bad request for manuscripts`() {
        searchTitle("*", MaterialType.MANUSCRIPT)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title-search endpoint should return 400 bad request for monographs`() {
        searchTitle("*", MaterialType.MONOGRAPH)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title-search endpoint should return 400 bad request for periodicals`() {
        searchTitle("*", MaterialType.PERIODICAL)
            .expectStatus().isBadRequest
    }

    @Test
    fun `get-title-search endpoint should return empty flux when no items match search term`() {
        every { collectionsRepository.getTitleByName("no match") } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        searchTitle("no match", MaterialType.NEWSPAPER)
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `search-item endpoint should return 200 OK with all items`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isOk
            .returnResult<Item>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNextCount(collectionsModelMockItemA.getObjects()!!.size.toLong())
            .verifyComplete()
    }

    @Test
    fun `search-item endpoint should return mapped items`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(
                Item(
                    catalogueId = collectionsModelMockItemA.getFirstObject()!!.priRef,
                    name = collectionsModelMockItemA.getFirstObject()!!.getName(),
                    date = collectionsModelMockItemA.getFirstObject()!!.getItemDate(),
                    materialType = collectionsModelMockItemA.getFirstObject()!!.getMaterialTypeFromParent()!!.norwegian,
                    titleCatalogueId = collectionsModelMockItemA.getFirstObject()!!.getTitleCatalogueId(),
                    titleName = collectionsModelMockItemA.getFirstObject()!!.getTitleName(),
                    digital = collectionsModelMockItemA.getFirstObject()!!.getFormat() == CollectionsFormat.DIGITAL,
                    urn = collectionsModelMockItemA.getFirstObject()!!.getUrn()
                )
            )
            .expectComplete()
        verify(exactly = 1) { collectionsRepository.getSingleCollectionsModel(any()) }
    }

    @Test
    fun `search-item endpoint should return 400 bad request for manuscripts`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.MANUSCRIPT)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `search-item endpoint should return 400 bad request for monographs`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.MONOGRAPH)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `search-item endpoint should return 400 bad request for periodicals`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.PERIODICAL)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `search-item endpoint should return empty flux when no items match search term`() {
        every { collectionsRepository.getWorkYearForTitle("no match", any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "no match")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .returnResult<Item>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `search-item endpoint should return 400 bad request if date is invalid`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "9999-99-99")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `search-item endpoint should return 400 bad request if required request params are omitted`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest

        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }
}
