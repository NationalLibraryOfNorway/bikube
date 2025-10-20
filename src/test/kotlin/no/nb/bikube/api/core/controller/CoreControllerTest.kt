package no.nb.bikube.api.core.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.api.newspaper.NewspaperMockData
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.exception.NotSupportedException
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.service.SearchFilterService
import no.nb.bikube.newspaper.service.NewspaperService
import no.nb.bikube.newspaper.service.TitleIndexService
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
    private lateinit var newspaperService: NewspaperService

    @MockkBean
    private lateinit var titleIndexService: TitleIndexService

    @MockkBean
    private lateinit var searchFilterService: SearchFilterService

    @Test
    fun `get single item for newspaper should return item in body`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.just(NewspaperMockData.Companion.newspaperItemMockA.copy())

        coreController.getSingleItem("1", MaterialType.NEWSPAPER).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.Companion.newspaperItemMockA, it)
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
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(NewspaperMockData.Companion.newspaperTitleMockA.copy())

        coreController.getSingleTitle("1", MaterialType.NEWSPAPER).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.Companion.newspaperTitleMockA, it)
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
        every { titleIndexService.searchTitle(any()) } returns listOf(
            NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
        )

        every { searchFilterService.filterSearchResults(any(), any(), any(), any()) } returns listOf(
            NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
        )

        Assertions.assertEquals(
            coreController.searchTitle("Avis", MaterialType.NEWSPAPER).body!!,
            listOf(
                NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
            )
        )
    }

    @Test
    fun `search title should throw NotSupportedException when trying to search for anything other than NEWSPAPER`() {
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.MANUSCRIPT) }
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.PERIODICAL) }
        assertThrows<NotSupportedException> { coreController.searchTitle("Avis", MaterialType.MONOGRAPH) }

        verify(exactly = 0) { titleIndexService.searchTitle(any()) }
    }

    @Test
    fun `search title should call on titleIndexService function when materialType is NEWSPAPER`() {
        every { titleIndexService.searchTitle(any()) } returns emptyList()
        every { searchFilterService.filterSearchResults(any(), any(), any(), any()) } returns emptyList()

        Assertions.assertEquals(
            coreController.searchTitle("Avis", MaterialType.NEWSPAPER).body!!,
            emptyList<Title>()
        )

        verify(exactly = 1) { titleIndexService.searchTitle(any()) }
    }

    @Test
    fun `search title should call on searchFilterService function when materialType is NEWSPAPER`() {
        every { titleIndexService.searchTitle(any()) } returns listOf(
            NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
        )
        every { searchFilterService.filterSearchResults(any(), any(), any(), any()) } returns listOf(
            NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
        )

        Assertions.assertEquals(
            coreController.searchTitle("Avis", MaterialType.NEWSPAPER).body!!,
            listOf(
                NewspaperMockData.Companion.newspaperTitleMockA.copy(), NewspaperMockData.Companion.newspaperTitleMockB.copy()
            )
        )
        verify(exactly = 1) { searchFilterService.filterSearchResults(any(), any(), any(), any()) }
    }

    @Test
    fun `search item should return a list of items matching criteria`() {
        every { newspaperService.getItemsByTitleAndDate(any(), any(), any()) } returns Flux.just(
            NewspaperMockData.Companion.newspaperItemMockA.copy(), NewspaperMockData.Companion.newspaperItemMockA.copy()
        )

        coreController.searchItem("1", MaterialType.NEWSPAPER, "2020-01-01", true).body!!
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.Companion.newspaperItemMockA, it)
            }
            .assertNext {
                Assertions.assertEquals(NewspaperMockData.Companion.newspaperItemMockA, it)
            }
            .verifyComplete()
    }

    @Test
    fun `search item should throw NotSupportedException when trying to search for anything other than NEWSPAPER`() {
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.MANUSCRIPT, "2020-01-01", false) }
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.PERIODICAL, "2020-01-01", false) }
        assertThrows<NotSupportedException> { coreController.searchItem("Avis", MaterialType.MONOGRAPH, "2020-01-01", false) }

        verify { newspaperService.getItemsByTitleAndDate(any(), any(), any()) wasNot Called }
    }

    @Test
    fun `search item should call on newspaperService function when materialType is NEWSPAPER`() {
        every { newspaperService.getItemsByTitleAndDate(any(), any(), any()) } returns Flux.empty()

        coreController.searchItem("1", MaterialType.NEWSPAPER, "2020-01-01", true).body!!
            .test()
            .verifyComplete()

        verify(exactly = 1) { newspaperService.getItemsByTitleAndDate(any(), any(), any()) }
    }

    @Test
    fun `search item should throw BadRequestBodyException when searchTerm is empty`() {
        assertThrows<BadRequestBodyException> { coreController.searchItem("", MaterialType.NEWSPAPER, "2020-01-01", false) }

        verify { newspaperService.getItemsByTitleAndDate(any(), any(), any()) wasNot Called }
    }
}
