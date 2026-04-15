package no.nb.bikube.hugin.model.dto

import java.time.LocalDate

data class CreateBoxDto(
    val titleId: Int,
    val id: String? = null,
    val dateFrom: LocalDate
)
