package no.nb.bikube.api.core.model

import java.time.LocalDate

data class Item(
    val catalogueId: String,
    val name: String?,
    val date: LocalDate?,
    val materialType: String?,
    val titleCatalogueId: String?,
    val titleName: String?,
    val digital: Boolean?,
    val urn: String?,
    val location: String? = null,
    val parentCatalogueId: String?
) : CatalogueRecord
