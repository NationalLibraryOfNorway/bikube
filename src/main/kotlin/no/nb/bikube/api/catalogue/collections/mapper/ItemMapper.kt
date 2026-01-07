package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.enum.CollectionsFormat
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.core.util.DateUtils.Companion.parseYearOrDate

fun mapCollectionsObjectToGenericItem(model: CollectionsObject): Item {
    return Item(
        catalogueId = model.priRef,
        name = model.getName(),
        date = model.getDate() ?: model.getParentDate(),
        materialType = model.getMaterialTypeFromParent()?.norwegian,
        titleCatalogueId = model.getTitleCatalogueId(),
        titleName = model.getTitleName(),
        digital = model.getFormat()?.let { model.getFormat() == CollectionsFormat.DIGITAL },
        urn = model.getUrn(),
        location = model.locationBarcode,
        parentCatalogueId = model.getParentId()
    )
}

fun mapCollectionsPartsObjectToGenericItem(
    model: CollectionsPartsReference,
    titleCatalogueId: String?,
    titleName: String?,
    materialType: String?,
    date: String? = null
): Item {
    return Item(
        catalogueId = model.priRef,
        name = model.getName(),
        date = parseYearOrDate(date) ?: model.getDate(),
        materialType = materialType,
        titleCatalogueId = titleCatalogueId,
        titleName = titleName,
        digital = model.getFormat() == CollectionsFormat.DIGITAL,
        urn = model.urn?.first(),
        parentCatalogueId = null
    )
}
