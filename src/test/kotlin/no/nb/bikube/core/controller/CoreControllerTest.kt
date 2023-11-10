package no.nb.bikube.core.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.service.CollectionsService
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
    private lateinit var collectionsService: CollectionsService

    @Test
    fun `get single item for newspaper should return item in body`() {
        every { collectionsService.getSingleItem(any()) } returns Mono.just(newspaperItemMockA.copy())

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
        every { collectionsService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA.copy())

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
    fun `search title should return a list of titles matching name`() {
        every { collectionsService.searchTitleByName(any()) } returns Flux.just(
            newspaperTitleMockA.copy(), newspaperTitleMockB.copy()
        )

        coreController.searchTitle("Avis", MaterialType.NEWSPAPER).body!!
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

    @Test
    fun `search title should throw NotSupportedException when trying to search for anything other than NEWSPAPER`() {
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.MANUSCRIPT) }
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.PERIODICAL) }
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.MONOGRAPH) }

        verify { collectionsService.searchTitleByName(any()) wasNot Called }
    }

    @Test
    fun `search title should call on collectionsService function when materialType is NEWSPAPER`() {
        every { collectionsService.searchTitleByName(any()) } returns Flux.empty()

        coreController.searchTitle("Avis", MaterialType.NEWSPAPER).body!!
            .test()
            .verifyComplete()

        verify { collectionsService.searchTitleByName(any()) }
    }

    @Test
    fun `search item should return a list of items matching criteria`() {
        every { collectionsService.getItemsByTitle(any(), any(), any(), any()) } returns Flux.just(
            newspaperItemMockA.copy(), newspaperItemMockA.copy()
        )

        coreController.searchItem("1", MaterialType.NEWSPAPER, "2020-01-01", true).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(newspaperItemMockA, it)
            }
            .assertNext {
                Assertions.assertEquals(newspaperItemMockA, it)
            }
            .verifyComplete()
    }

    @Test
    fun `search item should throw NotSupportedException when trying to search for anything other than NEWSPAPER`() {
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.MANUSCRIPT, "2020-01-01", false) }
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.PERIODICAL, "2020-01-01", false) }
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.MONOGRAPH, "2020-01-01", false) }

        verify { collectionsService.getItemsByTitle(any(), any(), any(), any()) wasNot Called }
    }

    @Test
    fun `search item should call on collectionsService function when materialType is NEWSPAPER`() {
        every { collectionsService.getItemsByTitle(any(), any(), any(), any()) } returns Flux.empty()

        coreController.searchItem("1", MaterialType.NEWSPAPER, "2020-01-01", true).body!!
            .test()
            .verifyComplete()

        verify { collectionsService.getItemsByTitle(any(), any(), any(), any()) }
    }

    @Test
    fun `search item should throw BadRequestBodyException when searchTerm is empty`() {
        assertThrows<BadRequestBodyException> { coreController.searchItem("", MaterialType.NEWSPAPER, "2020-01-01", false) }

        verify { collectionsService.getItemsByTitle(any(), any(), any(), any()) wasNot Called }
    }
}
