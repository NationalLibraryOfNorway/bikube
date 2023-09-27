package no.nb.bikube.newspaper.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(private val axiellConfig: AxiellConfig) {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().baseUrl(axiellConfig.url).build()
    }
}