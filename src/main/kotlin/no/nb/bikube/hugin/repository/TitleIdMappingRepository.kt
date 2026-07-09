package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.dbo.TitleIdMapping
import org.springframework.data.jpa.repository.JpaRepository

interface TitleIdMappingRepository : JpaRepository<TitleIdMapping, Int>
