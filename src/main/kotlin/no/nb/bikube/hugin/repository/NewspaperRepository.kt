package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.dbo.Newspaper
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface NewspaperRepository : JpaRepository<Newspaper, String> {
    fun findTopByBoxIdOrderByDateDesc(boxId: String): Newspaper?
    fun existsByBoxIdAndDate(boxId: String, date: LocalDate): Boolean
}
