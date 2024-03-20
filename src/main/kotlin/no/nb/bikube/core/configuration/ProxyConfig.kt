package no.nb.bikube.core.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("http-proxy")
class ProxyConfig(
    val host: String,
    val port: Int
)
