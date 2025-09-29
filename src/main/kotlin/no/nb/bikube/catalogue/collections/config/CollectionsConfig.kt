package no.nb.bikube.catalogue.collections.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilder
import java.net.URL

@ConfigurationProperties("collections")
class CollectionsConfig (
    val url: String,
    val directLink: String,
    val linkTemplate: UriBuilder = DefaultUriBuilderFactory().uriString(directLink)
)
