package no.nb.bikube.core.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.service.AxiellService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class CoreControllerTest {
    @Autowired
    private lateinit var coreController: CoreController

    @MockkBean
    private lateinit var axiellService: AxiellService

    @Test
    fun `get single item for newspaper should return item in body`() {
        every { axiellService.getSingleItem(any()) } returns Mono.just(newspaperItemMockA.copy())

        coreController.getSingleItem("1", MaterialType.NEWSPAPER).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperItemMockA, it)
            }
            .verifyComplete()
    }

    @Test
    fun `get single item should throw error when trying to get manuscripts`() {
        assertThrows<NotSupportedException> { coreController.getSingleItem("1", MaterialType.MANUSCRIPT) }
    }

    @Test
    fun `get single item should throw error when trying to get periodicals`() {
        assertThrows<NotSupportedException> { coreController.getSingleItem("1", MaterialType.PERIODICAL) }
    }

    @Test
    fun `get single item should throw error when trying to get monographs`() {
        assertThrows<NotSupportedException> { coreController.getSingleItem("1", MaterialType.MONOGRAPH) }
    }

    @Test
    fun `get single title for newspaper should return title in body`() {
        every { axiellService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())

        coreController.getSingleTitle("1", MaterialType.NEWSPAPER).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it)
            }
            .verifyComplete()
    }

    @Test
    fun `get single title should throw error when trying to get manuscripts`() {
        assertThrows<NotSupportedException> { coreController.getSingleTitle("1", MaterialType.MANUSCRIPT) }
    }

    @Test
    fun `get single title should throw error when trying to get periodicals`() {
        assertThrows<NotSupportedException> { coreController.getSingleTitle("1", MaterialType.PERIODICAL) }
    }

    @Test
    fun `get single title should throw error when trying to get monographs`() {
        assertThrows<NotSupportedException> { coreController.getSingleTitle("1", MaterialType.MONOGRAPH) }
    }

    @Test
    fun `search should return a list of titles matching name`() {
        every { axiellService.searchTitleByName(any()) } returns Flux.just(
            newspaperTitleMockA.copy(), newspaperTitleMockB.copy()
        )

        coreController.search("Avis", MaterialType.NEWSPAPER).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockA, it)
            }
            .assertNext {
                Assertions.assertEquals(newspaperTitleMockB, it)
            }
            .verifyComplete()
    }
}
