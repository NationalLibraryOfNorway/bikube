package no.nb.bikube.newspaper.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.core.service.CreationValidationService
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.service.NewspaperService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class ItemControllerTest {
    @Autowired
    private lateinit var itemController: ItemController

    @MockkBean
    private lateinit var collectionsService: NewspaperService

    @MockkBean
    private lateinit var creationValidationService: CreationValidationService

    @Test
    fun `create item should return 200 OK with created item`() {
        every { collectionsService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())
        every { collectionsService.createNewspaperItem(any()) } returns Mono.just(newspaperItemMockA.copy())
        every { creationValidationService.validateItem(any()) } returns Unit

        itemController.createItem(newspaperItemMockCValidForCreation.copy())
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(
                    newspaperItemMockA.copy(),
                    it.body
                )
            }
            .verifyComplete()
    }
}
