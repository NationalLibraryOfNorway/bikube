package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsSeriesObject
import no.nb.bikube.api.catalogue.collections.model.getEndDate
import no.nb.bikube.api.catalogue.collections.model.getName
import no.nb.bikube.api.catalogue.collections.model.getStartDate
import no.nb.bikube.api.core.model.Title

fun mapCollectionsSeriesObjectToGenericTitle(model: CollectionsSeriesObject): Title {
    return Title(
        name = model.getName(),
        startDate = model.getStartDate(),
        endDate = model.getEndDate(),
        publisher = model.publisher,
        publisherPlace = model.placeOfPublication,
        language = model.language,
        materialType = null,
        catalogueId = model.priRef,
    )
}
