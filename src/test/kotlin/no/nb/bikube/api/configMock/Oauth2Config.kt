package no.nb.bikube.configMock

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.security.oauth2.client.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType

@Configuration
@AutoConfigureBefore(ReactiveOAuth2ClientAutoConfiguration::class)
class MockOAuth2TestConfig {

    @Bean
    fun clientRegistrationRepository(): ReactiveClientRegistrationRepository {
        val clientRegistration = ClientRegistration.withRegistrationId("keycloak")
            .clientId("test-client")
            .clientSecret("test-secret")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("http://localhost:8099/token")
            .build()
        return InMemoryReactiveClientRegistrationRepository(clientRegistration)
    }

    @Bean
    fun reactiveOAuth2AuthorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientManager {
        val clientService: ReactiveOAuth2AuthorizedClientService =
            InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository)
        val manager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository, clientService
        )
        return manager
    }
}
