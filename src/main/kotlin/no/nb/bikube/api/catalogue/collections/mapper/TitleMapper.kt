package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.core.model.RelatedTitle
import no.nb.bikube.api.core.model.Title


fun mapCollectionsObjectToGenericTitle(model: CollectionsObject): Title {
    return Title(
        name = model.getName(),
        startDate = model.getStartDate(),
        endDate = model.getEndDate(),
        publisher = model.getPublisher(),
        publisherPlace = model.getPublisherPlace(),
        language = model.getLanguage(),
        materialType = model.getMaterialType()?.norwegian,
        catalogueId = model.priRef,
        relatedTitles = model.relatedObjectList?.map { mapRelatedObject(it) }
    )
}

fun mapRelatedObject(relatedObject: CollectionsRelatedObject): RelatedTitle {
    return RelatedTitle(
        catalogueId = relatedObject.priRef,
        title = relatedObject.title,
        association = relatedObject.association,
        recordType = relatedObject.recordTypeList?.firstOrNull { it.lang == "neutral" }?.text
    )
}
