package no.nb.bikube.configuration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest
@Import(SpaRoutingConfig::class)
class SpaRoutingConfigTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `deep client-side route returns index html`() {
        client.mutateWith(mockOidcLogin())
            .get().uri("/hugin/titles/123")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType("text/html")
    }
}
