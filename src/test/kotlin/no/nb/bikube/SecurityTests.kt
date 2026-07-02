package no.nb.bikube

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.api.newspaper.service.NewspaperService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper

@Import(TestcontainersConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@TestPropertySource(properties = ["security.enabled=true"])
class SecurityTests {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @MockkBean
    private lateinit var newspaperService: NewspaperService

    private lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        client = WebTestClient
            .bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    @Test
    fun `should not allow access to get endpoints without login`() {
        client.get()
            .uri("/api/item?catalogueId=123&materialType=${MaterialType.NEWSPAPER.name}")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should not allow access to get api docs without login`() {
        client.get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should not allow access to post endpoints without login`() {
        client.post()
            .uri("/api/newspapers/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should allow access to post endpoints when logged in`() {
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA)
        every { newspaperService.createNewspaperItem(any()) } returns Mono.just(newspaperItemMockA)

        client.mutateWith(mockJwt())
            .post()
            .uri("/api/newspapers/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `should return 401 on post for invalid token`() {
        client.post()
            .uri("/api/newspapers/items")
            .header("Authorization", "Bearer eyIkkeEnToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
            .exchange()
            .expectStatus().isUnauthorized
    }
}
