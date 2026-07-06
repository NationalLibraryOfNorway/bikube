package no.nb.bikube.hugin.controller

import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.configuration.PermissiveSecurityConfig
import no.nb.bikube.hugin.repository.BoxRepository
import no.nb.bikube.hugin.repository.NewspaperRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Optional

@WebFluxTest(HuginNewspaperController::class)
@Import(PermissiveSecurityConfig::class)
class HuginNewspaperControllerTest {

    @Autowired
    lateinit var client: WebTestClient

    @MockitoBean
    lateinit var clientRegistrationRepository: ReactiveClientRegistrationRepository

    @MockitoBean
    lateinit var titleRepository: TitleRepository

    @MockitoBean
    lateinit var boxRepository: BoxRepository

    @MockitoBean
    lateinit var newspaperRepository: NewspaperRepository

    @MockitoBean
    lateinit var newspaperService: NewspaperService

    @Test
    fun `GET titles returns 404 when title does not exist`() {
        whenever(titleRepository.findById(99)).thenReturn(Optional.empty())

        client.get().uri("/api/hugin/titles/99")
            .exchange()
            .expectStatus().isNotFound
    }
}
