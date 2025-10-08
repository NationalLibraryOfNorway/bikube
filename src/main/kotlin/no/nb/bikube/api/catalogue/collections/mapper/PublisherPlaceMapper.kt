package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsTermObject
import no.nb.bikube.api.core.model.PublisherPlace

fun mapCollectionsObjectToGenericPublisherPlace(model: CollectionsTermObject): PublisherPlace {
    return PublisherPlace(
        name = model.term,
        catalogueId = model.priRef
    )
}
