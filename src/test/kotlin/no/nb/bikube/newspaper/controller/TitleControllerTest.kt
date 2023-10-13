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
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.service.AxiellService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class TitleControllerTest {
    @Autowired
    private lateinit var titleController: TitleController

    @MockkBean
    private lateinit var axiellService: AxiellService

    @Test
    fun `getTitles should return 200 OK with list of titles`() {
        every { axiellService.getTitles() } returns Flux.just(newspaperTitleMockA.copy())
        titleController.getTitles().body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return 200 OK with the created title`() {
        every { axiellService.createPublisher(any()) } returns Mono.empty()
        every { axiellService.createPublisherPlace(any()) } returns Mono.empty()
        every { axiellService.createLanguage(any()) } returns Mono.empty()
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())

        titleController.createTitle(newspaperTitleMockA)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return 400 bad request if request body object title is null or empty`() {
        titleController.createTitle(newspaperTitleMockA.copy(name = null))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Title name cannot be null or empty"
            }
            .verify()

        titleController.createTitle(newspaperTitleMockA.copy(name = ""))
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
        titleController.createTitle(newspaperTitleMockA.copy(startDate = startDate, endDate = endDate))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Start date cannot be after end date"
            }
            .verify()
    }

    @Test
    fun `createTitle should call create publisher if publisher is present on request body`() {
        every { axiellService.createPublisher(any()) } returns Mono.just(Publisher("Pub", "1"))
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(publisher = "Pub", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createPublisherPlace if publisherPlace is present on request body`() {
        every { axiellService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Pub", "1"))
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(publisherPlace = "Pub"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createLanguage if language is present on request body`() {
        every { axiellService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(language = "nob", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should not call on createPublisher, createPublisherPlace or createLanguage if not present on request body`() {
        val title = newspaperTitleMockA.copy(publisher = null, publisherPlace = null, language = null)
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(title)
        titleController.createTitle(title)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(title, it.body)
            }
            .verifyComplete()

        verify { axiellService.createPublisher(any()) wasNot Called }
        verify { axiellService.createPublisherPlace(any()) wasNot Called }
        verify { axiellService.createLanguage(any()) wasNot Called }
    }

    @Test
    fun `createTitle should continue when createPublisher receives a 409 conflict`() {
        every { axiellService.createPublisher(any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Schibsted' already exists")
        )
        every { axiellService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { axiellService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(publisher = "Schibsted"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createPublisherPlace receives a 409 conflict`() {
        every { axiellService.createPublisher(any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { axiellService.createPublisherPlace(any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Oslo' already exists")
        )
        every { axiellService.createLanguage(any()) } returns Mono.just(Language("nob", "1"))
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(publisherPlace = "Oslo"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createLanguage receives a 409 conflict`() {
        every { axiellService.createPublisher(any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { axiellService.createPublisherPlace(any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { axiellService.createLanguage(any()) } returns Mono.error(
            RecordAlreadyExistsException("Language 'nob' already exists")
        )
        every { axiellService.createNewspaperTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        titleController.createTitle(newspaperTitleMockA.copy(language = "nob"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

}
