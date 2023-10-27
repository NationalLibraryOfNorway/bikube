package no.nb.bikube.newspaper.config

import org.apache.hc.client5.http.auth.AuthSchemeFactory
import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.NTCredentials
import org.apache.hc.client5.http.auth.StandardAuthScheme
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.cookie.BasicCookieStore
import org.apache.hc.client5.http.impl.async.HttpAsyncClients
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager
import org.apache.hc.core5.http.config.RegistryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import java.util.*


@Configuration
class WebClientConfig(private val axiellConfig: AxiellConfig) {

    @Bean
    @Throws(NoClassDefFoundError::class)
    fun webClient(): WebClient {
        val connector = initHttpClientConnector()

        return WebClient.builder().clientConnector(connector).baseUrl(axiellConfig.url).build()
    }

    private fun initHttpClientConnector(): HttpComponentsClientHttpConnector {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(
            AuthScope(null, null, -1, null, null),
            NTCredentials(axiellConfig.username, axiellConfig.password.toCharArray(), null, null)
        )
        val schemeFactoryRegistry = RegistryBuilder
            .create<AuthSchemeFactory>()
            .register(StandardAuthScheme.NTLM, NTLMSchemeFactory.INSTANCE)
            .build()
        val config = RequestConfig
            .custom()
            .setTargetPreferredAuthSchemes(listOf(
                StandardAuthScheme.NTLM, StandardAuthScheme.KERBEROS)
            )
            .build()
        val client = HttpAsyncClients.custom().apply {
            setDefaultCredentialsProvider(credentialsProvider)
            setDefaultAuthSchemeRegistry(schemeFactoryRegistry)
            setConnectionManager(PoolingAsyncClientConnectionManager())
            setDefaultCookieStore(BasicCookieStore())
            setDefaultRequestConfig(config)
        }.build()
        return HttpComponentsClientHttpConnector(client)
    }
}