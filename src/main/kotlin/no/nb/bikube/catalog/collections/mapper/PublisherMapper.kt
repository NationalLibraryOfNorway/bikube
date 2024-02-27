package no.nb.bikube.catalog.collections.mapper

import no.nb.bikube.catalog.collections.model.CollectionsNameObject
import no.nb.bikube.core.model.Publisher

fun mapCollectionsObjectToGenericPublisher(model: CollectionsNameObject): Publisher {
    return Publisher(
        name = model.name,
        databaseId = model.priRef
    )
}
