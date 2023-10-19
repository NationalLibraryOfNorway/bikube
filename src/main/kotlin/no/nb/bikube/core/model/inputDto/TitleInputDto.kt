package no.nb.bikube.core.model.inputDto

import java.time.LocalDate

data class TitleInputDto (
    val name: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val publisher: String?,
    val publisherPlace: String?,
    val language: String?
)
