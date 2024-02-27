package no.nb.bikube.catalog.collections.mapper

import no.nb.bikube.catalog.collections.model.CollectionsTermObject
import no.nb.bikube.core.model.Language

fun mapCollectionsObjectToGenericLanguage(model: CollectionsTermObject): Language {
    return Language(
        name = model.term,
        databaseId = model.priRef
    )
}
