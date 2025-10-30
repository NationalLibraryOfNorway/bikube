package no.nb.bikube.api.core.model

data class Publisher(
    val name: String?,
    val catalogueId: String?
) : CatalogueRecord
