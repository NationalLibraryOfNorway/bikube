package no.nb.bikube.core.mapper

import no.nb.bikube.core.enum.CollectionsFormat
import no.nb.bikube.core.model.Item
import no.nb.bikube.core.model.collections.*

fun mapCollectionsObjectToGenericItem(model: CollectionsObject): Item {
    return Item(
        catalogueId = model.priRef,
        name = model.getName(),
        date = model.getItemDate(),
        materialType = model.getMaterialTypeFromParent()?.norwegian,
        titleCatalogueId = model.getTitleCatalogueId(),
        titleName = model.getTitleName(),
        digital = model.getFormat() == CollectionsFormat.DIGITAL,
        urn = model.getUrn()
    )
}

fun mapCollectionsPartsObjectToGenericItem(
    model: CollectionsPartsReference,
    titleCatalogueId: String?,
    titleName: String?,
    materialType: String?
): Item {
    return Item(
        catalogueId = model.priRef!!,
        name = model.getName(),
        date = model.getItemDate(),
        materialType = materialType,
        titleCatalogueId = titleCatalogueId,
        titleName = titleName,
        digital = model.getFormat() == CollectionsFormat.DIGITAL,
        urn = null
    )
}
