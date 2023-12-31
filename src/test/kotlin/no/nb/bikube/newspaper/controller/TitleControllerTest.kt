package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.RecordAlreadyExistsException
import no.nb.bikube.core.model.Language
import no.nb.bikube.core.model.Publisher
import no.nb.bikube.core.model.PublisherPlace
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleInputDtoMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.service.CollectionsService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class TitleControllerTest {
    @Autowired
    private lateinit var titleController: TitleController

    @MockkBean
    private lateinit var collectionsService: CollectionsService

    @Test
    fun `createTitle should return 200 OK with the created title`() {
        every { collectionsService.createPublisher(any()) } returns Mono.empty()
        every { collectionsService.createPublisherPlace(any()) } returns Mono.empty()
        every { collectionsService.createLanguage(any()) } returns Mono.empty()
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())

        titleController.createTitle(newspaperTitleInputDtoMockA)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return 400 bad request if request body object title is null or empty`() {
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(name = null))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Title name cannot be null or empty"
            }
            .verify()

        titleController.createTitle(newspaperTitleInputDtoMockA.copy(name = ""))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Title name cannot be null or empty"
            }
            .verify()
    }

    @Test
    fun `createTitle should return BadRequestBodyException if startDate is after endDate`() {
        val startDate = LocalDate.of(2020, 1, 1)
        val endDate = LocalDate.of(2019, 1, 1)
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(startDate = startDate, endDate = endDate))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Start date cannot be after end date"
            }
            .verify()
    }

    @Test
    fun `createTitle should call create publisher if publisher is present on request body`() {
        every { collectionsService.createPublisher(any()) } returns Mono.just(Publisher("Pub", "1"))
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(publisher = "Pub", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createPublisherPlace if publisherPlace is present on request body`() {
        every { collectionsService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Pub", "1"))
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Pub"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createLanguage if language is present on request body`() {
        every { collectionsService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(language = "nob", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should not call on createPublisher, createPublisherPlace or createLanguage if not present on request body`() {
        val titleInput = newspaperTitleInputDtoMockA.copy(publisher = null, publisherPlace = null, language = null)
        val title = newspaperTitleMockA.copy(publisher = null, publisherPlace = null, language = null)
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(title)
        titleController.createTitle(titleInput)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(title, it.body)
            }
            .verifyComplete()

        verify { collectionsService.createPublisher(any()) wasNot Called }
        verify { collectionsService.createPublisherPlace(any()) wasNot Called }
        verify { collectionsService.createLanguage(any()) wasNot Called }
    }

    @Test
    fun `createTitle should continue when createPublisher receives a 409 conflict`() {
        every { collectionsService.createPublisher(any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Schibsted' already exists")
        )
        every { collectionsService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { collectionsService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(publisher = "Schibsted"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createPublisherPlace receives a 409 conflict`() {
        every { collectionsService.createPublisher(any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { collectionsService.createPublisherPlace(any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Oslo' already exists")
        )
        every { collectionsService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(publisherPlace = "Oslo"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createLanguage receives a 409 conflict`() {
        every { collectionsService.createPublisher(any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { collectionsService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { collectionsService.createLanguage(any()) } returns Mono.error(
            RecordAlreadyExistsException("Language 'nob' already exists")
        )
        every { collectionsService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleInputDtoMockA.copy(language = "nob"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

}
