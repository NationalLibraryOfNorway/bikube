package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsTermObject
import no.nb.bikube.core.model.PublisherPlace

fun mapCollectionsObjectToGenericPublisherPlace(model: CollectionsTermObject): PublisherPlace {
    return PublisherPlace(
        name = model.term,
        databaseId = model.priRef
    )
}