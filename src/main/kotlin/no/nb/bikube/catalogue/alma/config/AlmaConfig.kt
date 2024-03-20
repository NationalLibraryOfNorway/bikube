package no.nb.bikube.catalogue.alma.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("alma")
class AlmaConfig(
    val almawsUrl: String,
    val apiKey: String
)
