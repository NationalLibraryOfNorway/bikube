package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsObject
import no.nb.bikube.core.model.Language

fun mapCollectionsObjectToGenericLanguage(model: CollectionsObject): Language {
    return Language(
        name = model.term,
        catalogueId = model.priRef
    )
}