package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
import no.nb.bikube.hugin.model.Title
import no.nb.bikube.hugin.repository.TitleRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

@BrowserCallable
class HuginNewspaperService(
    private val titleRepository: TitleRepository
) {

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    fun getTitlesByTitle(title: String): List<Title> {
        var titles = titleRepository.findAllByVendorContainingIgnoreCase(title)
        return titles;
    }

}
