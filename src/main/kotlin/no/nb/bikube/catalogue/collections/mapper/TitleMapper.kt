package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.model.*
import no.nb.bikube.core.model.Title


fun mapCollectionsObjectToGenericTitle(model: CollectionsObject): Title {
    return Title(
        name = model.getName(),
        startDate = model.getStartDate(),
        endDate = model.getEndDate(),
        publisher = model.getPublisher(),
        publisherPlace = model.getPublisherPlace(),
        language = model.getLanguage(),
        materialType = model.getMaterialType()?.norwegian,
        catalogueId = model.priRef
    )
}
