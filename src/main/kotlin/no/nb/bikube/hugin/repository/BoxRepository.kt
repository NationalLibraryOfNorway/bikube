package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.Box
import org.springframework.data.jpa.repository.JpaRepository

interface BoxRepository : JpaRepository<Box, String>
