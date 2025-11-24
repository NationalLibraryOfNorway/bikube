package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.model.CollectionsNameObject
import no.nb.bikube.core.model.Publisher

fun mapCollectionsObjectToGenericPublisher(model: CollectionsNameObject): Publisher {
    return Publisher(
        name = model.name,
        catalogueId = model.priRef
    )
}
