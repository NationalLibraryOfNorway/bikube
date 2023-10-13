package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.collections.CollectionsObject
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.util.DateUtils.Companion.parseYearOrDate


fun mapCollectionsObjectToGenericTitle(model: CollectionsObject): Title {
    return Title(
        name = model.titleList?.first()?.title,
        startDate = model.datingList?.first()?.dateFrom?.let { parseYearOrDate(it) },
        endDate = model.datingList?.first()?.dateTo?.let { parseYearOrDate(it) },
        publisher = model.publisherList?.first(),
        publisherPlace = model.placeOfPublicationList?.first(),
        language = model.languageList?.first()?.language,
        materialType = model.subMediumList?.first()?.subMedium,
        catalogueId = model.priRef
    )
}
