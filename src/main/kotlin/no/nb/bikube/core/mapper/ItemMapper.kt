package no.nb.bikube.core.mapper

import no.nb.bikube.core.enum.AxiellFormat
import no.nb.bikube.core.model.CollectionsObject
import no.nb.bikube.core.model.Item

fun mapCollectionsObjectToGenericItem(model: CollectionsObject): Item {
    return Item(
        catalogueId = model.priRef,
        name = model.title?.first()?.title,
        date = null,
        materialType = model.partOf?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.subMedium?.first()?.subMedium,
        titleCatalogueId = model.partOf?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.priRef,
        titleName = model.partOf?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.partOfGroup?.first()?.partOfReference?.title?.first()?.title,
        digital = model.format?.first()?.first { it.lang == "neutral" }?.text == AxiellFormat.DIGITAL.value
    )
}
