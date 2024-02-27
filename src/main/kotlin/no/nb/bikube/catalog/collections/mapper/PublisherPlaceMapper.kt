package no.nb.bikube.catalog.collections.mapper

import no.nb.bikube.catalog.collections.model.CollectionsTermObject
import no.nb.bikube.core.model.PublisherPlace

fun mapCollectionsObjectToGenericPublisherPlace(model: CollectionsTermObject): PublisherPlace {
    return PublisherPlace(
        name = model.term,
        databaseId = model.priRef
    )
}
