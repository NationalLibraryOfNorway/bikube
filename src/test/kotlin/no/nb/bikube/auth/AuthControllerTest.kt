package no.nb.bikube.auth

import no.nb.bikube.configuration.PermissiveSecurityConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(AuthController::class)
@Import(PermissiveSecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @MockitoBean
    lateinit var clientRegistrationRepository: ReactiveClientRegistrationRepository

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
