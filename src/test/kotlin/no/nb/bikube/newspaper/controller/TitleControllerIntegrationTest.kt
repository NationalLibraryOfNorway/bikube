package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleC
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsNameModelMockA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsNameModelWithEmptyRecordListA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA
import no.nb.bikube.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.catalogue.collections.service.CollectionsService
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.model.inputDto.TitleInputDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.service.UniqueIdService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.Duration
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TitleControllerIntegrationTest (
    @Autowired private var webClient: WebTestClient
){
    @MockkBean
    private lateinit var collectionsService: CollectionsService

    @MockkBean
    private lateinit var uniqueIdService: UniqueIdService

    private val titleId = "1"

    private fun createTitle(title: TitleInputDto): ResponseSpec {
        return webClient
            .post()
            .uri("/newspapers/titles/")
            .bodyValue(title)
            .exchange()
    }

    @BeforeEach
    fun beforeEach() {
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(1000)).build()

        every { collectionsService.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsService.getSingleCollectionsModel("2") } returns Mono.just(collectionsModelMockTitleC.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren(titleId) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { collectionsService.getSingleCollectionsModelWithoutChildren("2") } returns Mono.just(collectionsModelMockTitleC.copy())
        every { collectionsService.createNewspaperRecord(any()) } returns Mono.just(collectionsModelMockTitleC.copy())
        every { collectionsService.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA.copy())
        every { collectionsService.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB.copy())
        every { collectionsService.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA.copy())
        every { collectionsService.createNameRecord(any(), CollectionsDatabase.PEOPLE) } returns Mono.just(collectionsNameModelMockA.copy())
        every { collectionsService.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) } returns Mono.just(collectionsTermModelMockLocationB.copy())
        every { collectionsService.createTermRecord(any(), CollectionsDatabase.LANGUAGES) } returns Mono.just(collectionsTermModelMockLanguageA.copy())
        every { uniqueIdService.getUniqueId() } returns "123"
    }

    @Test
    fun `post-newspapers-titles should return 201 created with title`() {
        createTitle(newspaperTitleInputDtoMockA)
            .expectStatus().isCreated
    }

    @Test
    fun `post-newspapers-titles should return correctly mapped title`() {
        createTitle(newspaperTitleInputDtoMockB)
            .expectStatus().isCreated
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNext(newspaperTitleMockB)
            .verifyComplete()
    }

    @Test
    fun `post-newspapers-titles should return 400 bad request if title is empty`() {
        createTitle(newspaperTitleInputDtoMockA.copy(name = ""))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-titles should use publisher if present and in Collections`() {
        every { collectionsService.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchPublisher(any()) }
        verify(exactly = 0) { collectionsService.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should create publisher if present and not in Collections`() {
        every { collectionsService.searchPublisher(any()) } returns Mono.just(collectionsNameModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchPublisher(any()) }
        verify(exactly = 1) { collectionsService.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisher if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(publisher = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsService.searchPublisher(any()) }
        verify(exactly = 0) { collectionsService.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should use publisherPlace if present and in Collections`() {
        every { collectionsService.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchPublisherPlace(any()) }
        verify(exactly = 0) { collectionsService.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should create publisherPlace if present and not in Collections`() {
        every { collectionsService.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchPublisherPlace(any()) }
        verify(exactly = 1) { collectionsService.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisherPlace if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsService.searchPublisherPlace(any()) }
        verify(exactly = 0) { collectionsService.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should use language if present and in Collections`() {
        every { collectionsService.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchLanguage(any()) }
        verify(exactly = 0) { collectionsService.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should create language if present and not in Collections`() {
        every { collectionsService.searchLanguage(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsService.searchLanguage(any()) }
        verify(exactly = 1) { collectionsService.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should ignore language if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(language = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsService.searchLanguage(any()) }
        verify(exactly = 0) { collectionsService.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should return 400 bad request if start date is after end date`() {
        createTitle(newspaperTitleInputDtoMockA.copy(
            startDate = LocalDate.parse("2023-12-12"),
            endDate = LocalDate.parse("2020-12-12")
        ))
            .expectStatus().isBadRequest
    }
}
