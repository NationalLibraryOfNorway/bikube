package no.nb.bikube.api.core.model.inputDto

import java.time.LocalDate

data class ItemInputDto(
    val date: LocalDate,
    val titleCatalogueId: String,
    val username: String,
    val digital: Boolean? = false,
    val urn: String? = null,
    val containerId: String? = null,
    val notes: String? = null,
    val number: String? = null,
    val volume: String? = null,
    val version: String? = null,
    val itemStatus: String? = null
)
