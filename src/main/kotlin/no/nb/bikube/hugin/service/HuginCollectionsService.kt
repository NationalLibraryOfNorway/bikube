package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import kotlinx.coroutines.awaitAll
import no.nb.bikube.api.core.model.Title
import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.api.newspaper.service.TitleIndexService
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@BrowserCallable
class HuginCollectionsService(
    private val titleIndexService: TitleIndexService,
    private val newspaperService: NewspaperService,
) {

    private val webClient = WebClient
        .builder()
        .baseUrl("/")
        .build()

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    fun findByTitle(searchTerm: String): List<Title> {
        val searchResult = titleIndexService.searchTitle(searchTerm)
        return searchResult;
    }

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    fun findById(catalogueId: String): Title? {
        return newspaperService.getSingleTitle(catalogueId).block();
    }

}
