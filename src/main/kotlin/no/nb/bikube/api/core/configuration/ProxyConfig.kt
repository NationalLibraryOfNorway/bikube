package no.nb.bikube.api.core.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("http-proxy")
class ProxyConfig(
    val host: String,
    val port: Int
)
