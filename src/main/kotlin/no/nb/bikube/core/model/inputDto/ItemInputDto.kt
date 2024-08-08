package no.nb.bikube.core.model.inputDto

import java.time.LocalDate

data class ItemInputDto(
    val date: LocalDate,
    val titleCatalogueId: String,
    val username: String,
    val digital: Boolean? = false,
    val urn: String? = null,
    val name: String? = null,
    val containerId: String? = null,
    val notes: String? = null
)
