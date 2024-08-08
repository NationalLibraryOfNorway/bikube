package no.nb.bikube.core.model.inputDto

import java.time.LocalDate

data class MissingPeriodicalItemDto(
    val date: LocalDate,
    val titleCatalogueId: String,
    val username: String,
    val notes: String? = null
)
