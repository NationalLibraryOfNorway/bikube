package no.nb.bikube.core.model

import java.time.LocalDate

data class Title (
    val name: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val publisher: String?,
    val publisherPlace: String?,
    val language: String?,
    val materialType: String?,
    val catalogueId: String?
)
