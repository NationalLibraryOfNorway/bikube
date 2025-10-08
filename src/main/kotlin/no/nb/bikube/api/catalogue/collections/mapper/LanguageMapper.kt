package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.model.CollectionsTermObject
import no.nb.bikube.api.core.model.Language

fun mapCollectionsObjectToGenericLanguage(model: CollectionsTermObject): Language {
    return Language(
        name = model.term,
        catalogueId = model.priRef
    )
}
