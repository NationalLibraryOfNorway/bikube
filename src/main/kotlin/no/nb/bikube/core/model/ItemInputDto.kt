package no.nb.bikube.core.model

import java.time.LocalDate

data class ItemInputDto(
    val date: LocalDate?,
    val titleCatalogueId: String?,
    val digital: Boolean?,
    val urn: String?,
)
