package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsObject
import no.nb.bikube.core.model.Title


fun mapCollectionsObjectToGenericTitle(model: CollectionsObject): Title {
    return Title(
        name = model.title[0].title,
        startDate = null,
        endDate = null,
        publisher = null,
        language = null,
        materialType = null,
        catalogueId = model.priRef
    )
}
