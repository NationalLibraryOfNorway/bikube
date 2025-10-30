package no.nb.bikube.api.core.model

import java.time.LocalDate

data class Title(
    val catalogueId: String,
    val name: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val publisher: String?,
    val publisherPlace: String?,
    val language: String?,
    val materialType: String?
) : CatalogueRecord
