package no.nb.bikube.api.catalogue.alma.config

import no.nb.bikube.api.core.configuration.ProxyConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.ProxyProvider

@Configuration
class AlmaHttpConnector(
    private val proxyConfig: ProxyConfig
) {

    @Bean
    fun httpConnector(): ClientHttpConnector {
        val client = HttpClient.create()
            .followRedirect(true)
        if (proxyConfig.host.isEmpty())
            return ReactorClientHttpConnector(client)
        return ReactorClientHttpConnector(
            client.proxy { proxy ->
                proxy.type(ProxyProvider.Proxy.HTTP)
                    .host(proxyConfig.host)
                    .port(proxyConfig.port)
            }
        )
    }
}
