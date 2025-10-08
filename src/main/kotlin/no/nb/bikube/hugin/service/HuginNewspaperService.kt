package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.RolesAllowed
import no.nb.bikube.hugin.model.HuginTitle
import no.nb.bikube.hugin.repository.TitleRepository

@BrowserCallable
class HuginNewspaperService(
    private val titleRepository: TitleRepository
) {

    @RolesAllowed("T_dimo_admin", "T_dimo_user")
    fun getTitlesByTitle(title: String): List<HuginTitle> {
        var titles = titleRepository.findAllByVendorContainingIgnoreCase(title)
        return titles;
    }

}
