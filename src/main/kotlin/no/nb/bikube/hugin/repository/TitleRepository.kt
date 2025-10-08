package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.Title
import org.springframework.data.jpa.repository.JpaRepository

interface TitleRepository : JpaRepository<Title, Int> {
    fun findAllByVendorContainingIgnoreCase(title: String): List<Title>
}

