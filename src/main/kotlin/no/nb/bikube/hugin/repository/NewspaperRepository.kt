package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.Newspaper
import org.springframework.data.jpa.repository.JpaRepository

interface NewspaperRepository : JpaRepository<Newspaper, String>

