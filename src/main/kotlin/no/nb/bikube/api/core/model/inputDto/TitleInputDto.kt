package no.nb.bikube.api.core.model.inputDto

import java.time.LocalDate

data class TitleInputDto (
    val name: String,
    val username: String,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val publisher: String? = null,
    val publisherPlace: String? = null,
    val language: String? = null,
)
