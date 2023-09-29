package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelEmptyRecordListMock
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleB
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleC
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleD
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleE
import no.nb.bikube.core.enum.AxiellDescriptionType
import no.nb.bikube.core.enum.AxiellRecordType
import no.nb.bikube.core.exception.AxiellCollectionsException
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.model.TitleDto
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class AxiellServiceTest {
    @Autowired
    private lateinit var axiellService: AxiellService

    @MockkBean
    private lateinit var axiellRepository: AxiellRepository

    @Test
    fun `getTitle should fetch data as CollectionModel from repo and convert to title`() {
        every { axiellRepository.getAllTitles() } returns Mono.just(collectionsModelMockTitleE)

        axiellService.getTitles()
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()
    }

    @Test
    fun `createTitle should return Title object with default values from Title with only name and materialType`() {
        every { axiellRepository.createTitle(any()) } returns Mono.just(collectionsModelMockTitleE)

        val body = Title(
            newspaperTitleMockB.name,
            null,
            null,
            null,
            null,
            null,
            newspaperTitleMockB.materialType,
            null
        )
        val encodedValue = Json.encodeToString(TitleDto(
            title = newspaperTitleMockB.name!!,
            recordType = AxiellRecordType.WORK.value,
            descriptionType = AxiellDescriptionType.SERIAL.value,
            subMedium = newspaperTitleMockB.materialType
        ))

        axiellService.createTitle(body)
            .test()
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()

        verify { axiellRepository.createTitle(encodedValue) }
    }

    @Test
    fun `createTitle should throw exception with error message from repository method`() {
        every { axiellRepository.createTitle(any()) } returns Mono.error(AxiellCollectionsException("Error creating title"))

        axiellService.createTitle(newspaperTitleMockB)
            .test()
            .expectErrorMatches { it is AxiellCollectionsException && it.message == "Error creating title" }
            .verify()
    }

    @Test
    fun `getItemsForTitle should return all items for title`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleA.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return title information on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleA.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.titleCatalogueId == collectionsModelMockTitleA.adlibJson.recordList.first().priRef
                        && it.titleName == collectionsModelMockTitleA.adlibJson.recordList.first().titleList!!.first().title
            }
            .expectNextMatches {
                it.titleCatalogueId == collectionsModelMockTitleA.adlibJson.recordList.first().priRef
                        && it.titleName == collectionsModelMockTitleA.adlibJson.recordList.first().titleList!!.first().title
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return material type on items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleA.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleA.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextMatches {
                it.materialType == collectionsModelMockTitleA.adlibJson.recordList.first().subMediumList!!.first().subMedium
            }
            .expectNextMatches {
                it.materialType == collectionsModelMockTitleA.adlibJson.recordList.first().subMediumList!!.first().subMedium
            }
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when serial work has no year works`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleB.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleB.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when year work has no manifestations`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleC.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleC.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux when manifestation has no items`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelMockTitleD.copy())

        axiellService.getItemsForTitle(collectionsModelMockTitleD.adlibJson.recordList.first().priRef)
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

    @Test
    fun `getItemsForTitle should return empty flux if title does not exist`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.just(collectionsModelEmptyRecordListMock.copy())

        axiellService.getItemsForTitle("123")
            .test()
            .expectSubscription()
            .expectNextCount(0)
            .verifyComplete()
    }

}
