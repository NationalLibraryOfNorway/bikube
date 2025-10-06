package no.nb.bikube.catalogue.collections.config

import io.netty.handler.timeout.TimeoutException
import no.nb.bikube.core.util.logger
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
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
    fun reactiveOAuth2AuthorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveOAuth2AuthorizedClientManager {
        val authorizedClientProvider: ReactiveOAuth2AuthorizedClientProvider =
            ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build()

        val authorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository)
        )
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}

@Configuration
class CollectionsWebClientConfig(
    private val collectionsConfig: CollectionsConfig,
    private val authorizedClientManager: ReactiveOAuth2AuthorizedClientManager
) {

    @Bean
    fun collectionsWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30))

        val oauth = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oauth.setDefaultClientRegistrationId("keycloak") // Uses the 'keycloak' client registration as configured in application properties

        val webClientBuilder: Builder = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .baseUrl(collectionsConfig.url)
            .filter(oauth)
            .filter { request, next -> // Logging filter runs after OAuth2 filter
                logger().info("Request URL: ${request.url()}")
                request.headers().forEach { name, values ->
                    logger().info("Header: $name = $values")
                }
                logger().info(request.body().toString())
                next.exchange(request)
            }
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
