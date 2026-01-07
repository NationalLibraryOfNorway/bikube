package no.nb.bikube.api.catalogue.collections.config

import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class CollectionsServiceConfig (
    val collectionsWebClient: CollectionsWebClientConfig
) {
    @Bean
    @Qualifier("collectionsNewspaperService")
    fun collectionsNewspaperService(): CollectionsService {
        return CollectionsService(collectionsWebClient, CollectionsDatabase.NEWSPAPER)
    }

    // Example of additional service for another database, disabled via profile by default.
    @Bean
    @Qualifier("collectionsEphemeraService")
    @Profile("enable-ephemera-service")
    fun collectionsEphemeraService(): CollectionsService {
        return CollectionsService(collectionsWebClient, CollectionsDatabase.EPHEMERA)
    }
}
