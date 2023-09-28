package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockTitleE
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockB
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockC
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
@ActiveProfiles("test")
class AxiellServiceTest {
    @Autowired private lateinit var axiellService: AxiellService

    @MockkBean private lateinit var axiellRepository: AxiellRepository

    @Test
    fun `should get all titles as collection model from repo and convert to title`() {
        every { axiellRepository.getTitles() } returns Mono.just(collectionsModelMockTitleE)

        val result = axiellService.getTitles()

        StepVerifier.create(result)
            .expectNextMatches { it == newspaperTitleMockB }
            .verifyComplete()
    }

    @Test
    fun `should create title from title object with default values for recordType and descriptionType`() {
        val expected = newspaperTitleMockC
        val body = newspaperTitleMockC.copy(language = "neutral")
//        val titleDto = TitleDto(
//            newspaperTitleMockB.name!!,
//            "Work",
//            "Serial",
//            newspaperTitleMockB.materialType
//        )

        every { axiellRepository.createTitle(any()) } returns Mono.just(collectionsModelMockTitleE)

        val result = axiellService.createTitle(body)

        StepVerifier.create(result)
            .expectNextMatches { it == expected }
            .verifyComplete()
    }
}