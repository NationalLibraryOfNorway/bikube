package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.CollectionsTermObject
import no.nb.bikube.core.model.Language

fun mapCollectionsObjectToGenericLanguage(model: CollectionsTermObject): Language {
    return Language(
        name = model.term,
        databaseId = model.priRef
    )
}