package no.nb.bikube.api.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import no.nb.bikube.api.newspaper.NewspaperMockData
import no.nb.bikube.api.catalogue.collections.mapper.mapCollectionsObjectToGenericTitle
import no.nb.bikube.api.newspaper.controller.TitleController
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(
    properties = [
        "search-index.enabled=true",
        "search-index.path=index-data-test",
    ]
)
class TitleIndexServiceTest(
    @Autowired private val titleIndexService: TitleIndexService,
    @Autowired private val titleController: TitleController
) {
    @MockkBean
    private lateinit var newspaperService: NewspaperService

    @BeforeAll
    fun mockTitleList() {
        every { newspaperService.getAllTitles() } returns
                Mono.just(
                    CollectionsModelMockData.collectionsModelMockAllTitles
                        .getObjects()!!
                        .map { mapCollectionsObjectToGenericTitle(it) }
                )
        titleIndexService.indexAllTitles()
    }

    @Test
    fun `All titles should be indexed and searchable`() {
        Assertions.assertEquals(
            titleIndexService.searchTitle("avis").size,
            3
        )
    }

    @Test
    fun `A newly created title should be searchable immediately`() {
        Assertions.assertEquals(
            titleIndexService.searchTitle("Unique title").size,
            0
        )
        every { newspaperService.createPublisher(any(), any()) } returns Mono.empty()
        every { newspaperService.createPublisherPlace(any(), any()) } returns Mono.empty()
        every { newspaperService.createLanguage(any(), any()) } returns Mono.empty()
        every { newspaperService.createNewspaperTitle(any()) } returns
                Mono.just(NewspaperMockData.newspaperTitleMockA.copy(name = "Unique title"))

        titleController.createTitle(NewspaperMockData.newspaperTitleInputDtoMockA)
            .subscribe()

        Assertions.assertEquals(
            titleIndexService.searchTitle("Unique title").size,
            1
        )
    }
}
