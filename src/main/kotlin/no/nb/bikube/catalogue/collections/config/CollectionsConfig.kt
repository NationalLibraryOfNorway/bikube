package no.nb.bikube.catalogue.collections.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilder
import java.net.URL

@ConfigurationProperties("collections")
class CollectionsConfig (
    val url: String,
    val username: String,
    val password: String,
    var directLinkTemplate: UriBuilder = DefaultUriBuilderFactory().builder()
) {
    init {
        val urlObject = URL(url)
        directLinkTemplate = directLinkTemplate
            .scheme(urlObject.protocol)
            .host(urlObject.host)
            .pathSegment("collections_UAT", "link", "xplus", "textscatalogue", "{id}")
    }
}
