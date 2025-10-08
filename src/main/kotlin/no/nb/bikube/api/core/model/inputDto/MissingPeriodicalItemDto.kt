package no.nb.bikube.api.core.model.inputDto

import java.time.LocalDate

data class MissingPeriodicalItemDto(
    val date: LocalDate,
    val titleCatalogueId: String,
    val username: String,
    val notes: String? = null,
    val volume: String? = null,
    val number: String? = null,
    val version: String? = null
)
