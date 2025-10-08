package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsNameObject
import no.nb.bikube.api.core.model.Publisher

fun mapCollectionsObjectToGenericPublisher(model: CollectionsNameObject): Publisher {
    return Publisher(
        name = model.name,
        catalogueId = model.priRef
    )
}
