package no.nb.bikube.api.core.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockAllTitles
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartsObjectMockItemA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsPartsObjectMockManifestationA
import no.nb.bikube.api.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.api.catalogue.collections.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.model.Title
import no.nb.bikube.api.core.util.DateUtils
import no.nb.bikube.api.newspaper.service.TitleIndexService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.ProblemDetail
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
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CoreControllerIntegrationTest (
    @Autowired private var webClient: WebTestClient
){
    @MockkBean
    private lateinit var collectionsService: CollectionsService

    @MockkBean
    private lateinit var titleIndexService: TitleIndexService

    private val titleId = "1"
    private val manifestationId = "3"
    private val itemId = "4"

    @BeforeEach
    fun beforeEach() {
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(60)).build()

        every { collectionsService.getSingleCollectionsModel(collectionsModelMockManifestationA.getFirstId()!!) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren(titleId) } returns Mono.just(collectionsModelMockTitleA.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren(manifestationId) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren(itemId) } returns Mono.just(collectionsModelMockItemA.copy())
        every { collectionsService.getManifestations(any(), any(), any(), any(), any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsService.getManifestations(any(), titleId, any(), any(), any()) } returns Mono.just(collectionsModelMockManifestationA.copy())
        every { titleIndexService.searchTitle(any()) } returns
                collectionsModelMockAllTitles.getObjects()!!.map { mapCollectionsObjectToGenericTitle(it) }
    }

    private fun getItem(itemId: String, materialType: MaterialType): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item")
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
                uri.pathSegment("api", "title")
                    .queryParam("catalogueId", titleId)
                    .queryParam("materialType", materialType)
                    .build()
            }
            .exchange()
    }

    private fun searchTitle(
        search: String,
        materialType: MaterialType,
        date: LocalDate? = null,
        selectBestMatch: Boolean? = null
    ): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "title", "search")
                    .queryParam("searchTerm", search)
                    .queryParam("materialType", materialType)
                    .apply {
                        if (date != null) {
                            queryParam("date", date)
                        }
                        if (selectBestMatch != null) {
                            queryParam("selectBestMatch", selectBestMatch)
                        }
                    }
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
        val testItem = collectionsModelMockItemA.getFirstObject()

        getItem(itemId, MaterialType.NEWSPAPER)
            .expectStatus().isOk
            .returnResult<Item>()
            .responseBody
            .test()
            .expectNext(
                Item(
                    catalogueId = testItem.priRef,
                    name = testItem.getName(),
                    date = testItem.getDate(),
                    materialType = testItem.getMaterialTypeFromParent()!!.norwegian,
                    titleCatalogueId = testItem.getTitleCatalogueId(),
                    titleName = testItem.getTitleName(),
                    digital = testItem.getFormat() == CollectionsFormat.DIGITAL,
                    urn = testItem.getUrn(),
                    location = testItem.locationBarcode,
                    parentCatalogueId = testItem.getParentId()
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
    fun `get-item endpoint should return 404 with message when ID belongs to title or manifestation`() {
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
        val testTitle = collectionsModelMockTitleA.getFirstObject()

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
    fun `get-title endpoint should return 404 with message when ID belongs to manifestation or item`() {
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
        every { titleIndexService.searchTitle("no match") } returns emptyList()

        searchTitle("no match", MaterialType.NEWSPAPER)
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `get-title-search endpoint should return best match if query parameter is present`() {
        searchTitle("Bikubeavisen", MaterialType.NEWSPAPER, selectBestMatch = true)
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockTitleA.getFirstObject()))
            .verifyComplete()
    }

    @Test
    fun `get-title-search endpoint should filter by date if query parameter is present`() {
        searchTitle("Bikubeavisen", MaterialType.NEWSPAPER, date = LocalDate.parse("2021-01-01"))
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockTitleA.getFirstObject()))
            .expectNext(mapCollectionsObjectToGenericTitle(collectionsModelMockTitleB.getFirstObject()))
            .verifyComplete()
    }

    @Test
    fun `search-item endpoint should return 200 OK with all items`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
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
        val expectedItem = Item(
            catalogueId = collectionsPartsObjectMockItemA.partsReference!!.priRef,
            name = collectionsPartsObjectMockItemA.partsReference!!.titleList!!.first().title!!,
            date = DateUtils.parseYearOrDate(
                collectionsPartsObjectMockItemA.partsReference!!.titleList!!.first().title!!.takeLast(10)
            )!!,
            materialType = MaterialType.NEWSPAPER.value,
            titleCatalogueId = collectionsModelMockTitleA.getFirstId(),
            titleName = collectionsPartsObjectMockManifestationA.partsReference!!.titleList!!.first().title!!,
            digital = collectionsPartsObjectMockItemA.partsReference!!.getFormat() == CollectionsFormat.DIGITAL,
            urn = null,
            parentCatalogueId = null
        )

        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
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
            .expectNext(expectedItem)
            .verifyComplete()

        verify(exactly = 1) { collectionsService.getManifestations(any(), "1") }
        verify(exactly = 1) { collectionsService.getSingleCollectionsModel(collectionsModelMockManifestationA.getFirstId()!!) }
    }

    @Test
    fun `search-item endpoint should return 400 bad request for manuscripts`() {
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
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
                uri.pathSegment("api", "item", "search")
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
                uri.pathSegment("api", "item", "search")
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
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
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
                uri.pathSegment("api", "item", "search")
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
        // Missing date
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest

        // Missing title ID
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest

        // Missing material type
        webClient
            .get()
            .uri { uri ->
                uri.pathSegment("api", "item", "search")
                    .queryParam("titleCatalogueId", "1")
                    .queryParam("date", "2020-01-01")
                    .queryParam("isDigital", "true")
                    .build()
            }
            .exchange()
            .expectStatus().isBadRequest
    }
}
