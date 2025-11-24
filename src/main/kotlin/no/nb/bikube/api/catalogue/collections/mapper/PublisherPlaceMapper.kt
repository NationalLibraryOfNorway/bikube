package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.model.CollectionsTermObject
import no.nb.bikube.core.model.PublisherPlace

fun mapCollectionsObjectToGenericPublisherPlace(model: CollectionsTermObject): PublisherPlace {
    return PublisherPlace(
        name = model.term,
        catalogueId = model.priRef
    )
}
