package no.nb.bikube.api.catalogue.collections.config

import io.netty.handler.timeout.TimeoutException
import no.nb.bikube.api.core.util.logger
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.Builder
import org.springframework.web.reactive.function.client.WebClientRequestException
import reactor.netty.http.client.HttpClient
import reactor.util.retry.Retry
import reactor.util.retry.RetryBackoffSpec
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
            .responseTimeout(Duration.ofSeconds(30))

        val oauth = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oauth.setDefaultClientRegistrationId("keycloak-collections") // Uses the 'keycloak-collections' client registration as configured in application properties

        val webClientBuilder: Builder = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(collectionsConfig.url)
            .filter(oauth)
            .exchangeStrategies(
                ExchangeStrategies.builder().codecs { configurer ->
                    configurer.defaultCodecs().maxInMemorySize(64 * 1024 * 1024)
                }.build()
            )

        return webClientBuilder.build()
    }

    fun retryStrategy(functionName: String, mavisId: String): RetryBackoffSpec {
        return Retry.backoff(10, Duration.ofSeconds(5))
            .doBeforeRetry { logger().warn("$functionName failed, retrying in 5 seconds") }
            .doAfterRetry { logger().info("Retrying $functionName with mavisId $mavisId")}
            .filter { throwable -> throwable is WebClientRequestException && throwable.cause is TimeoutException }
    }
}
