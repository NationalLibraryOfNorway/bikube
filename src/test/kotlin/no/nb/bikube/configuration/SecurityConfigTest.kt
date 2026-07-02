package no.nb.bikube.configuration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `unauthenticated request to API returns 401 not redirect`() {
        client.get().uri("/api/hugin/titles/1")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `oauth2 login endpoint is publicly accessible`() {
        client.get().uri("/oauth2/authorization/keycloak-hugin")
            .exchange()
            .expectStatus().is3xxRedirection
    }
}
