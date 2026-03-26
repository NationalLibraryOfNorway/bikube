package no.nb.bikube.api.catalogue.collections.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import java.io.IOException
import java.time.Duration

@Profile("!test")
@Configuration
class KeycloakConfig {

    @Bean
    fun oAuth2AuthorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider: OAuth2AuthorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build()

        val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}

@Configuration
class CollectionsWebClientConfig(
    private val collectionsConfig: CollectionsConfig,
    private val authorizedClientManager: OAuth2AuthorizedClientManager
) {

    @Bean
    fun collectionsWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMinutes(2))

        val oauth = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oauth.setDefaultClientRegistrationId("keycloak-collections") // Uses the 'keycloak-collections' client registration as configured in application properties

        val retryStrategy = Retry.backoff(30, Duration.ofMillis(500))
            .maxBackoff(Duration.ofSeconds(5))
            .filter { it is IOException || it is WebClientResponseException.ServiceUnavailable }

        val errorOnServerError = ExchangeFilterFunction.ofResponseProcessor { response ->
            if (response.statusCode().is5xxServerError) response.createError()
            else Mono.just(response)
        }

        val retryFilter = ExchangeFilterFunction { request, next ->
            next.exchange(request).retryWhen(retryStrategy)
        }

        val webClientBuilder: Builder = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(collectionsConfig.url)
            .filter(oauth)
            .filter(errorOnServerError)
            .filter(retryFilter)
            .exchangeStrategies(
                ExchangeStrategies.builder().codecs { configurer ->
                    configurer.defaultCodecs().maxInMemorySize(64 * 1024 * 1024)
                }.build()
            )

        return webClientBuilder.build()
    }
}
