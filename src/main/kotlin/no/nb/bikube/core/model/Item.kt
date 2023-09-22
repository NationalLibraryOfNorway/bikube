package no.nb.bikube.core.model

import java.util.*

data class Item(
    val catalogueId: String,
    val name: String?,
    val date: Date?,
    val materialType: String?,
    val titleCatalogueId: String?,
    val titleName: String?,
    val digital: Boolean?,
)
