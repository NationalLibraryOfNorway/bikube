package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.dbo.HuginTitle
import org.springframework.data.jpa.repository.JpaRepository

interface TitleRepository : JpaRepository<HuginTitle, Int> {
    fun findAllByVendorContainingIgnoreCase(title: String): List<HuginTitle>
}
