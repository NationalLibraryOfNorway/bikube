package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.collections.CollectionsNameObject
import no.nb.bikube.core.model.Publisher

fun mapCollectionsObjectToGenericPublisher(model: CollectionsNameObject): Publisher {
    return Publisher(
        name = model.name,
        databaseId = model.priRef
    )
}
