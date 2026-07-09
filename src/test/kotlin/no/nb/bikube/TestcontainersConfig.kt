package no.nb.bikube

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.testcontainers.containers.PostgreSQLContainer

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfig {

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer<*> =
        PostgreSQLContainer("postgres:16-alpine")

    @Bean
    fun reactiveClientRegistrationRepository(): ReactiveClientRegistrationRepository {
        val huginReg = ClientRegistration.withRegistrationId("keycloak-hugin")
            .clientId("test-client")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/keycloak-hugin")
            .authorizationUri("http://localhost:9999/protocol/openid-connect/auth")
            .tokenUri("http://localhost:9999/protocol/openid-connect/token")
            .jwkSetUri("http://localhost:9999/protocol/openid-connect/certs")
            .scope("openid")
            .build()
        val collectionsReg = ClientRegistration.withRegistrationId("keycloak-collections")
            .clientId("test-collections-client")
            .clientSecret("test-secret")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("http://localhost:9999/protocol/openid-connect/token")
            .build()
        return InMemoryReactiveClientRegistrationRepository(huginReg, collectionsReg)
    }
}
