package no.nb.bikube.core.model.inputDto

import java.time.LocalDate

data class ItemInputDto(
    val date: LocalDate?,
    val titleCatalogueId: String?,
    val digital: Boolean?,
    val urn: String?,
    var title: String? = null
)
