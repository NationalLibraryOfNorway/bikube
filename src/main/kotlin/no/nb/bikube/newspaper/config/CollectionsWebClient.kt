package no.nb.bikube.newspaper.config

import com.kerb4j.client.SpnegoClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.WebClient
import java.net.URL
import java.util.*

@Configuration
class CollectionsWebClient(private val collectionsConfig: CollectionsConfig) {

    @Bean
    @Throws(NoClassDefFoundError::class)
    fun webClient(): WebClient {
        return WebClient.builder().baseUrl(collectionsConfig.url)
            .filter { request, next ->
                next.exchange(ClientRequest.from(request).headers { headers ->
                    headers.set(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                }.build())
            }.build()
    }

    private fun createAuthorizationHeader(): String {
        val spnegoClient = SpnegoClient.loginWithUsernamePassword(
            collectionsConfig.username,
            collectionsConfig.password,
            true
        )
        // Kerberos: Header starts with "Negotiate YII....."
        return spnegoClient.createContext(URL(collectionsConfig.url)).createTokenAsAuthroizationHeader()
    }
}