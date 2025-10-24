package no.nb.bikube.api.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.api.newspaper.NewspaperMockData
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.exception.RecordAlreadyExistsException
import no.nb.bikube.api.core.model.Language
import no.nb.bikube.api.core.model.Publisher
import no.nb.bikube.api.core.model.PublisherPlace
import no.nb.bikube.api.newspaper.service.NewspaperService
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
    private lateinit var newspaperService: NewspaperService

    @Test
    fun `createTitle should return 200 OK with the created title`() {
        every { newspaperService.createPublisher(any(), any()) } returns Mono.empty()
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.empty()
        every { newspaperService.createLanguage(any(), any()) } returns Mono.empty()
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())

        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return 400 bad request if request body object title is empty`() {
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(name = ""))
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
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(startDate = startDate, endDate = endDate))
            .test()
            .expectErrorMatches {
                it is BadRequestBodyException &&
                it.message == "Start date cannot be after end date"
            }
            .verify()
    }

    @Test
    fun `createTitle should call create publisher if publisher is present on request body`() {
        every { newspaperService.createPublisher(any(), any()) } returns Mono.just(Publisher("Pub", "1"))
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        every { newspaperService.createLanguage(any(), any()) } returns Mono.just(Language("nob", "1"))

        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(publisher = "Pub", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createPublisherPlace if publisherPlace is present on request body`() {
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.just(PublisherPlace("Pub", "1"))
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        every { newspaperService.createLanguage(any(), any()) } returns Mono.just(Language("nob", "1"))

        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(publisherPlace = "Pub"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should call createLanguage if language is present on request body`() {
        every { newspaperService.createLanguage(any(), any()) } returns Mono.just(Language("nob", "1"))
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(language = "nob", publisherPlace = null))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should not call on createPublisher, createPublisherPlace or createLanguage if not present on request body`() {
        val titleInput = NewspaperMockData.newspaperTitleInputDtoMockA.copy(publisher = null, publisherPlace = null, language = null)
        val title = NewspaperMockData.newspaperTitleMockA.copy(publisher = null, publisherPlace = null, language = null)
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(title)
        titleController.createTitle(titleInput)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(title, it.body)
            }
            .verifyComplete()

        verify { newspaperService.createPublisher(any(), any()) wasNot Called }
        verify { newspaperService.createPublisherPlace(any(), any()) wasNot Called }
        verify { newspaperService.createLanguage(any(), any()) wasNot Called }
    }

    @Test
    fun `createTitle should continue when createPublisher receives a 409 conflict`() {
        every { newspaperService.createPublisher(any(), any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Schibsted' already exists")
        )
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { newspaperService.createLanguage(any(), any()) } returns Mono.just(Language("nob", "1"))
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(publisher = "Schibsted"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createPublisherPlace receives a 409 conflict`() {
        every { newspaperService.createPublisher(any(), any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.error(
            RecordAlreadyExistsException("Publisher place 'Oslo' already exists")
        )
        every { newspaperService.createLanguage(any(), any()) } returns Mono.just(Language("nob", "1"))
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(publisherPlace = "Oslo"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

    @Test
    fun `createTitle should continue when createLanguage receives a 409 conflict`() {
        every { newspaperService.createPublisher(any(), any()) } returns Mono.just(Publisher("Schibsted", "1"))
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.just(PublisherPlace("Oslo", "1"))
        every { newspaperService.createLanguage(any(), any()) } returns Mono.error(
            RecordAlreadyExistsException("Language 'nob' already exists")
        )
        every { newspaperService.createNewspaperTitle(any()) } returns Mono.just(NewspaperMockData.newspaperTitleMockA.copy())
        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA.copy(language = "nob"))
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.newspaperTitleMockA, it.body)
            }
            .verifyComplete()
    }

}
