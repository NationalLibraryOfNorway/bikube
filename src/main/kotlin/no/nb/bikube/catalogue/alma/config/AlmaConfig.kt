package no.nb.bikube.catalogue.alma.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("alma")
class AlmaConfig(
    val almaSruUrl: String,
    val almaWsUrl: String,
    val apiKey: String
)
