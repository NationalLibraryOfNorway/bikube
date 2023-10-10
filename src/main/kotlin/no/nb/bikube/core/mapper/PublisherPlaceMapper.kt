package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsObject
import no.nb.bikube.core.model.PublisherPlace

fun mapCollectionsObjectToGenericPublisherPlace(model: CollectionsObject): PublisherPlace {
    return PublisherPlace(
        name = model.term,
        catalogueId = model.priRef
    )
}