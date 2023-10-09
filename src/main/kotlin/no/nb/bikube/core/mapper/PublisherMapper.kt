package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsObject
import no.nb.bikube.core.model.Publisher

fun mapCollectionsObjectToGenericPublisher(model: CollectionsObject): Publisher {
    return Publisher(
        name = model.name,
        catalogueId = model.priRef
    )
}