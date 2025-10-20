package no.nb.bikube.api.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.api.core.model.Title
import no.nb.bikube.api.core.model.inputDto.TitleInputDto
import no.nb.bikube.api.newspaper.NewspaperMockData
import no.nb.bikube.api.newspaper.service.UniqueIdService
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
    private lateinit var collectionsRepository: CollectionsRepository

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

        every { collectionsRepository.getSingleCollectionsModel(titleId) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModel("2") } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleC.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(titleId) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock.copy())
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren("2") } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsModelMockTitleC.copy())
        every { collectionsRepository.createNewspaperRecord(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsModelMockTitleC.copy())
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsNameModelMockA.copy())
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelMockLocationB.copy())
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA.copy())
        every { collectionsRepository.createNameRecord(any(), CollectionsDatabase.PEOPLE) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsNameModelMockA.copy())
        every { collectionsRepository.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsTermModelMockLocationB.copy())
        every { collectionsRepository.createTermRecord(any(), CollectionsDatabase.LANGUAGES) } returns Mono.just(
            CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA.copy())
        every { uniqueIdService.getUniqueId() } returns "123"
    }

    @Test
    fun `post-newspapers-titles should return 201 created with title`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA)
            .expectStatus().isCreated
    }

    @Test
    fun `post-newspapers-titles should return correctly mapped title`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockB)
            .expectStatus().isCreated
            .returnResult<Title>()
            .responseBody
            .test()
            .expectSubscription()
            .expectNext(NewspaperMockData.Companion.newspaperTitleMockB)
            .verifyComplete()
    }

    @Test
    fun `post-newspapers-titles should return 400 bad request if title is empty`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(name = ""))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-titles should use publisher if present and in Collections`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsNameModelMockA.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchPublisher(any()) }
        verify(exactly = 0) { collectionsRepository.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should create publisher if present and not in Collections`() {
        every { collectionsRepository.searchPublisher(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsNameModelWithEmptyRecordListA.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchPublisher(any()) }
        verify(exactly = 1) { collectionsRepository.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisher if not present`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisher = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsRepository.searchPublisher(any()) }
        verify(exactly = 0) { collectionsRepository.createNameRecord(any(), CollectionsDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should use publisherPlace if present and in Collections`() {
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelMockLocationB.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchPublisherPlace(any()) }
        verify(exactly = 0) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should create publisherPlace if present and not in Collections`() {
        every { collectionsRepository.searchPublisherPlace(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchPublisherPlace(any()) }
        verify(exactly = 1) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisherPlace if not present`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(publisherPlace = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsRepository.searchPublisherPlace(any()) }
        verify(exactly = 0) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.GEO_LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should use language if present and in Collections`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchLanguage(any()) }
        verify(exactly = 0) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should create language if present and not in Collections`() {
        every { collectionsRepository.searchLanguage(any()) } returns Mono.just(CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { collectionsRepository.searchLanguage(any()) }
        verify(exactly = 1) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should ignore language if not present`() {
        createTitle(NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(language = null))
            .expectStatus().isCreated

        verify(exactly = 0) { collectionsRepository.searchLanguage(any()) }
        verify(exactly = 0) { collectionsRepository.createTermRecord(any(), CollectionsDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should return 400 bad request if start date is after end date`() {
        createTitle(
            NewspaperMockData.Companion.newspaperTitleInputDtoMockA.copy(
            startDate = LocalDate.parse("2023-12-12"),
            endDate = LocalDate.parse("2020-12-12")
        ))
            .expectStatus().isBadRequest
    }
}
