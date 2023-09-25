package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.newspaper.service.AxiellService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class ItemControllerTest {
    @Autowired
    private lateinit var itemController: ItemController

    @MockkBean
    private lateinit var axiellService: AxiellService

    @Test
    fun `get items should return 200 OK with list of items`() {
        every { axiellService.getAllItems() } returns Flux.just(newspaperItemMockA.copy())
        itemController.getAllItems().body!!.test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperItemMockA, it)
            }
            .verifyComplete()
    }
}
