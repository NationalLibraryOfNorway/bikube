package no.nb.bikube.api.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.TestcontainersConfig
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockItemA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockManifestationA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsSeriesModelEmptyMock
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsSeriesModelMockTitleA
import no.nb.bikube.api.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import org.junit.jupiter.api.Test
import reactor.kotlin.test.test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono

@Import(TestcontainersConfig::class)
@SpringBootTest(properties = ["featureflag.series-manifestation=false"])
@ActiveProfiles("test")
class GetSingleTitleFlagOffTest {

    @Autowired private lateinit var newspaperService: NewspaperService

    @MockkBean(name = "collectionsNewspaperService")
    private lateinit var collectionsService: CollectionsService

    @MockkBean
    private lateinit var maxitService: MaxitService

    @Test
    fun `flag off - getSingleTitle should fetch WORK record from texts database`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns
            Mono.just(collectionsModelMockTitleA.copy())

        newspaperService.getSingleTitle("1").block()

        verify(exactly = 1) { collectionsService.getSingleCollectionsModelWithoutChildren("1") }
        verify(exactly = 0) { collectionsService.getSingleSeries(any()) }
    }

    @Test
    fun `flag off - getSingleTitle should throw not found when WORK record is missing`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns
            Mono.just(collectionsModelEmptyRecordListMock.copy())

        newspaperService.getSingleTitle("999")
            .test()
            .expectError()
            .verify()

        verify(exactly = 1) { collectionsService.getSingleCollectionsModelWithoutChildren("999") }
        verify(exactly = 0) { collectionsService.getSingleSeries(any()) }
    }

    @Test
    fun `flag off - getSingleTitle should throw CollectionsTitleNotFound if object is a manifestation`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren("1") } returns
            Mono.just(collectionsModelMockManifestationA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }

    @Test
    fun `flag off - getSingleTitle should throw CollectionsTitleNotFound if object is an item`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren("1") } returns
            Mono.just(collectionsModelMockItemA.copy())

        newspaperService.getSingleTitle("1")
            .test()
            .expectSubscription()
            .expectError(CollectionsTitleNotFound::class.java)
            .verify()
    }
}

@Import(TestcontainersConfig::class)
@SpringBootTest(properties = ["featureflag.series-manifestation=true"])
@ActiveProfiles("test")
class GetSingleTitleFlagOnTest {

    @Autowired private lateinit var newspaperService: NewspaperService

    @MockkBean(name = "collectionsNewspaperService")
    private lateinit var collectionsService: CollectionsService

    @MockkBean
    private lateinit var maxitService: MaxitService

    @Test
    fun `flag on - getSingleTitle should fetch from series database`() {
        every { collectionsService.getSingleSeries(any()) } returns
            Mono.just(collectionsSeriesModelMockTitleA.copy())

        newspaperService.getSingleTitle("1").block()

        verify(exactly = 1) { collectionsService.getSingleSeries("1") }
        verify(exactly = 0) { collectionsService.getSingleCollectionsModelWithoutChildren(any()) }
    }

    @Test
    fun `flag on - getSingleTitle should throw not found when series record is missing`() {
        every { collectionsService.getSingleSeries(any()) } returns
            Mono.just(collectionsSeriesModelEmptyMock.copy())

        newspaperService.getSingleTitle("999")
            .test()
            .expectError()
            .verify()

        verify(exactly = 1) { collectionsService.getSingleSeries("999") }
        verify(exactly = 0) { collectionsService.getSingleCollectionsModelWithoutChildren(any()) }
    }
}
