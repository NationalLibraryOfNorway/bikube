package no.nb.bikube

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.newspaper.service.NewspaperService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = ["security.enabled=true"])
class SecurityTests (
    @Autowired private val webClient: WebTestClient
){
    @MockkBean private lateinit var newspaperService: NewspaperService

    @Test
    fun `should allow access to get endpoints without login`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.just(newspaperItemMockA)

        webClient
            .get()
            .uri{ uri ->
                uri.path("/item")
                    .queryParam("catalogueId", "123")
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .build()
            }
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow access to get api docs without login`() {
        webClient
            .get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should not allow access to post endpoints without login`() {
        webClient
            .post()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemMockCValidForCreation)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    @WithMockUser(authorities = ["bikube-create"])
    fun `should allow access to post endpoints when logged in`() {
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA)
        every { newspaperService.createNewspaperItem(any()) } returns Mono.just(newspaperItemMockA)

        webClient
            .post()
            .uri("/newspapers/items")
            .bodyValue(newspaperItemMockCValidForCreation)
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `should return 401 on post for invalid token`() {
        webClient
            .post()
            .uri("/newspapers/items")
            .headers { it.setBearerAuth("eyIkkeEnToken") }
            .bodyValue(newspaperItemMockCValidForCreation)
            .exchange()
            .expectStatus().isUnauthorized
    }
}
