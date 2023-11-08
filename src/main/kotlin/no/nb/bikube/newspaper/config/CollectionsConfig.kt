package no.nb.bikube.newspaper.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("collections")
class CollectionsConfig (
    val url: String,
    val username: String,
    val password: String
)