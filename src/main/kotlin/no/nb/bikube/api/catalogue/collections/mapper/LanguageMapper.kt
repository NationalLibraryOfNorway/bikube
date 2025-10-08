package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.model.CollectionsTermObject
import no.nb.bikube.core.model.Language

fun mapCollectionsObjectToGenericLanguage(model: CollectionsTermObject): Language {
    return Language(
        name = model.term,
        catalogueId = model.priRef
    )
}
