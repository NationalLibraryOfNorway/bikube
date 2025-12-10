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
    val materialType: String?,
    val relatedTitles: List<RelatedTitle>? = null
) : CatalogueRecord

data class RelatedTitle(
    val catalogueId: String?,
    val title: String?,
    val association: String?,
    val recordType: String?
)

