package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleE
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsNameModelMockA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsNameModelWithEmptyRecordListA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelWithEmptyRecordListA
import no.nb.bikube.core.enum.AxiellDatabase
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.model.inputDto.TitleInputDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.repository.AxiellRepository
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
    private lateinit var axiellRepository: AxiellRepository

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
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(60)).build()

        every { axiellRepository.getSingleCollectionsModel(titleId) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())
        every { axiellRepository.createTextsRecord(any()) } returns Mono.just(collectionsModelMockTitleE.copy())
        every { axiellRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA.copy())
        every { axiellRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB.copy())
        every { axiellRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA.copy())
        every { axiellRepository.createNameRecord(any(), AxiellDatabase.PEOPLE) } returns Mono.just(collectionsNameModelMockA.copy())
        every { axiellRepository.createTermRecord(any(), AxiellDatabase.LOCATIONS) } returns Mono.just(collectionsTermModelMockLocationB.copy())
        every { axiellRepository.createTermRecord(any(), AxiellDatabase.LANGUAGES) } returns Mono.just(collectionsTermModelMockLanguageA.copy())
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
    fun `post-newspapers-titles should return 400 bad request if title is missing`() {
        createTitle(newspaperTitleInputDtoMockA.copy(name = null))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-titles should return 400 bad request if title is empty`() {
        createTitle(newspaperTitleInputDtoMockA.copy(name = ""))
            .expectStatus().isBadRequest
    }

    @Test
    fun `post-newspapers-titles should use publisher if present and in Collections`() {
        every { axiellRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelMockA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchPublisher(any()) }
        verify(exactly = 0) { axiellRepository.createNameRecord(any(), AxiellDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should create publisher if present and not in Collections`() {
        every { axiellRepository.searchPublisher(any()) } returns Mono.just(collectionsNameModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisher = "publisher"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchPublisher(any()) }
        verify(exactly = 1) { axiellRepository.createNameRecord(any(), AxiellDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisher if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(publisher = null))
            .expectStatus().isCreated

        verify(exactly = 0) { axiellRepository.searchPublisher(any()) }
        verify(exactly = 0) { axiellRepository.createNameRecord(any(), AxiellDatabase.PEOPLE) }
    }

    @Test
    fun `post-newspapers-titles should use publisherPlace if present and in Collections`() {
        every { axiellRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelMockLocationB.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchPublisherPlace(any()) }
        verify(exactly = 0) { axiellRepository.createTermRecord(any(), AxiellDatabase.LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should create publisherPlace if present and not in Collections`() {
        every { axiellRepository.searchPublisherPlace(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Mo"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchPublisherPlace(any()) }
        verify(exactly = 1) { axiellRepository.createTermRecord(any(), AxiellDatabase.LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should ignore publisherPlace if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = null))
            .expectStatus().isCreated

        verify(exactly = 0) { axiellRepository.searchPublisherPlace(any()) }
        verify(exactly = 0) { axiellRepository.createTermRecord(any(), AxiellDatabase.LOCATIONS) }
    }

    @Test
    fun `post-newspapers-titles should use language if present and in Collections`() {
        every { axiellRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelMockLanguageA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchLanguage(any()) }
        verify(exactly = 0) { axiellRepository.createTermRecord(any(), AxiellDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should create language if present and not in Collections`() {
        every { axiellRepository.searchLanguage(any()) } returns Mono.just(collectionsTermModelWithEmptyRecordListA.copy())

        createTitle(newspaperTitleInputDtoMockA.copy(language = "nob"))
            .expectStatus().isCreated

        verify(exactly = 1) { axiellRepository.searchLanguage(any()) }
        verify(exactly = 1) { axiellRepository.createTermRecord(any(), AxiellDatabase.LANGUAGES) }
    }

    @Test
    fun `post-newspapers-titles should ignore language if not present`() {
        createTitle(newspaperTitleInputDtoMockA.copy(language = null))
            .expectStatus().isCreated

        verify(exactly = 0) { axiellRepository.searchLanguage(any()) }
        verify(exactly = 0) { axiellRepository.createTermRecord(any(), AxiellDatabase.LANGUAGES) }
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
