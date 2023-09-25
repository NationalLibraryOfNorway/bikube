package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.service.AxiellService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux.just
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class TitleControllerTest {
    @Autowired
    private lateinit var titleController: TitleController

    @MockkBean
    private lateinit var axiellService: AxiellService

    @Test
    fun `get titles should return 200 OK with list of titles`() {
        every { axiellService.getTitles() } returns just(newspaperTitleMockA.copy())
        titleController.getTitles().body!!.test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it)
            }
            .verifyComplete()
    }
}
