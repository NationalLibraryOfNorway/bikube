package no.nb.bikube.api.configMock

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

@Configuration
@Profile("test")
@AutoConfigureBefore(ReactiveOAuth2ClientAutoConfiguration::class)
class MockOAuth2TestConfig {

    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository {
        val clientRegistration = ClientRegistration.withRegistrationId("keycloak")
            .clientId("test-client")
            .clientSecret("test-secret")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("http://localhost:8099/token")
            .build()
        return InMemoryClientRegistrationRepository(clientRegistration)
    }

    @Bean
    fun OAuth2AuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository
    ): OAuth2AuthorizedClientManager {
        val clientService: OAuth2AuthorizedClientService =
            InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        val manager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository, clientService
        )
        return manager
    }
}
