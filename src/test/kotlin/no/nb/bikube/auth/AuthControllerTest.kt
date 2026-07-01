package no.nb.bikube.auth

import no.nb.bikube.auth.model.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(AuthController::class)
class AuthControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `GET api auth me returns user info for authenticated OIDC user`() {
        client.mutateWith(mockOidcLogin().idToken { token ->
            token.claim("preferred_username", "testuser")
            token.claim("given_name", "Test")
            token.claim("family_name", "User")
            token.claim("email", "test@example.com")
        })
            .get().uri("/api/auth/me")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.username").isEqualTo("testuser")
            .jsonPath("$.email").isEqualTo("test@example.com")
    }

    @Test
    fun `GET api auth me returns 401 when unauthenticated`() {
        client.get().uri("/api/auth/me")
            .exchange()
            .expectStatus().isUnauthorized
    }
}
