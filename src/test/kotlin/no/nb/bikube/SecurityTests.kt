package no.nb.bikube

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.hugin.model.dbo.HuginTitle
import no.nb.bikube.hugin.repository.TitleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper
import java.net.URL
import java.util.Optional

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

    @MockkBean
    private lateinit var titleRepository: TitleRepository

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
    fun `should allow access to get endpoints without login`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.just(newspaperItemMockA)

        client.get()
            .uri("/api/item?catalogueId=123&materialType=${MaterialType.NEWSPAPER.name}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow GET access to title endpoint without login`() {
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA)

        client.get()
            .uri("/api/title?catalogueId=123&materialType=${MaterialType.NEWSPAPER.name}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow GET access to title search endpoint without login`() {
        client.get()
            .uri("/api/title/search?searchTerm=avisa&materialType=${MaterialType.NEWSPAPER.name}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow GET access to item search endpoint without login`() {
        every { newspaperService.getItemsByTitleAndDate(any(), any(), any()) } returns Flux.empty()

        client.get()
            .uri("/api/item/search?titleCatalogueId=123&materialType=${MaterialType.NEWSPAPER.name}&date=2020-01-01")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow GET access to title link endpoint without login`() {
        every { newspaperService.getLinkToSingleTitle(any()) } returns URL("https://example.com/title/123")

        client.get()
            .uri("/api/title/link?catalogueId=123&materialType=${MaterialType.NEWSPAPER.name}")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow access to api docs without login`() {
        client.get()
            .uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk
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
    fun `should allow access to post endpoints when logged in with bikube-create authority`() {
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA)
        every { newspaperService.createNewspaperItem(any()) } returns Mono.just(newspaperItemMockA)

        client.mutateWith(mockJwt().authorities(SimpleGrantedAuthority("bikube-create")))
            .post()
            .uri("/api/newspapers/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
            .exchange()
            .expectStatus().isCreated
    }

    // The catalogue write endpoints are for the bikube-create M2M client only. A hugin
    // admin session (T_dimo_admin/T_dimo_all) must not incidentally get write access here.
    // Hugin admins authenticate interactively via the SPA (oauth2Login), not Bearer JWT.
    @Test
    fun `should deny access to post endpoints for a hugin admin without bikube-create authority`() {
        client.mutateWith(mockOidcLogin().authorities(SimpleGrantedAuthority("T_dimo_admin")))
            .post()
            .uri("/api/newspapers/items")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
            .exchange()
            .expectStatus().isForbidden
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

    // The blanket GET permitAll on /api/** must not leak into /api/hugin/**, which is
    // restricted to DIMO staff via realm roles. These endpoints are excluded from the
    // GET permitAll rule and additionally gated by @PreAuthorize on the controller.
    @Test
    fun `should not allow GET access to hugin endpoints without login`() {
        client.get()
            .uri("/api/hugin/titles/1")
            .exchange()
            .expectStatus().isUnauthorized
    }

    // The bikube-create M2M client is for catalogue writes only; it must not incidentally
    // get access to the hugin admin endpoints, which are reserved for DIMO staff.
    @Test
    fun `should deny access to hugin endpoints for the bikube-create M2M client`() {
        every { titleRepository.findById(1) } returns Optional.of(HuginTitle().apply { id = 1 })

        client.mutateWith(mockJwt().authorities(SimpleGrantedAuthority("bikube-create")))
            .get()
            .uri("/api/hugin/titles/1")
            .exchange()
            .expectStatus().isForbidden
    }

    // Hugin admins authenticate interactively via the SPA (oauth2Login) with roles
    // sourced from the "groups" claim - not Bearer JWT, hence mockOidcLogin() here.
    @Test
    fun `should allow access to hugin endpoints for T_dimo_admin`() {
        every { titleRepository.findById(1) } returns Optional.of(HuginTitle().apply { id = 1 })

        client.mutateWith(mockOidcLogin().authorities(SimpleGrantedAuthority("T_dimo_admin")))
            .get()
            .uri("/api/hugin/titles/1")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `should allow access to hugin endpoints for T_dimo_all`() {
        every { titleRepository.findById(1) } returns Optional.of(HuginTitle().apply { id = 1 })

        client.mutateWith(mockOidcLogin().authorities(SimpleGrantedAuthority("T_dimo_all")))
            .get()
            .uri("/api/hugin/titles/1")
            .exchange()
            .expectStatus().isOk
    }

    // Verifies the realm_access -> GrantedAuthority mapping that @PreAuthorize relies on
    // to enforce the roles above. Without this converter, roles in the JWT are never
    // surfaced as authorities and every @PreAuthorize check silently fails closed
    // (or, if the converter is missing entirely, the annotation may not fire at all).
    @Test
    fun `realm role converter maps realm_access roles directly to authorities`() {
        val jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("token")
            .header("alg", "none")
            .issuedAt(java.time.Instant.now())
            .expiresAt(java.time.Instant.now().plusSeconds(60))
            .claim("realm_access", mapOf("roles" to listOf("T_dimo_admin", "bikube-create")))
            .build()

        val authorities = no.nb.bikube.configuration.realmRoleAuthoritiesConverter().convert(jwt)

        org.junit.jupiter.api.Assertions.assertEquals(
            setOf(SimpleGrantedAuthority("T_dimo_admin"), SimpleGrantedAuthority("bikube-create")),
            authorities?.toSet()
        )
    }

    // Interactive hugin logins (oauth2Login, not Bearer JWT) get their DIMO group
    // membership from the "groups" claim via this GrantedAuthoritiesMapper - it's what
    // ServerHttpSecurity's default oauth2Login authentication manager auto-detects from
    // the application context (see OAuth2LoginSpec.createDefault() in spring-security-config).
    // mockOidcLogin() in a WebTestClient test bypasses that manager entirely (it injects a
    // canned OidcUser directly), so this is verified as a pure unit test of the mapping logic.
    @Test
    fun `groups authorities mapper maps groups claim directly to authorities`() {
        val idToken = org.springframework.security.oauth2.core.oidc.OidcIdToken(
            "id-token-value",
            java.time.Instant.now(),
            java.time.Instant.now().plusSeconds(60),
            mapOf("sub" to "user-1")
        )
        val userInfo = org.springframework.security.oauth2.core.oidc.OidcUserInfo(
            mapOf("sub" to "user-1", "groups" to listOf("T_dimo_admin"))
        )
        val oidcUserAuthority = org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority(idToken, userInfo)

        val mapped = no.nb.bikube.configuration.groupsAuthoritiesMapper().mapAuthorities(listOf(oidcUserAuthority))

        org.junit.jupiter.api.Assertions.assertEquals(
            setOf(SimpleGrantedAuthority("T_dimo_admin")),
            mapped.toSet()
        )
    }

    // Confirms exactly one GrantedAuthoritiesMapper bean is registered - this is the bean
    // OAuth2LoginSpec.createDefault() looks up via getBeanOrNull(GrantedAuthoritiesMapper::class)
    // to wire authority mapping into the reactive oauth2Login authentication manager.
    @Test
    fun `exactly one GrantedAuthoritiesMapper bean is registered for oauth2Login to pick up`() {
        val mapper = applicationContext.getBean(
            org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper::class.java
        )

        org.junit.jupiter.api.Assertions.assertNotNull(mapper)
    }
}
