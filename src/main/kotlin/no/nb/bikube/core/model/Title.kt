package no.nb.bikube.core.model

import java.util.Date

data class Title (
    val name: String?,
    val startDate: Date?,
    val endDate: Date?,
    val publisher: String?,
    val language: String?,
    val materialType: String?,
    val catalogueId: String
)