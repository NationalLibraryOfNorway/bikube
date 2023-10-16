package no.nb.bikube.core.mapper

import no.nb.bikube.core.model.collections.CollectionsObject
import no.nb.bikube.core.model.collections.CollectionsPartsObject
import no.nb.bikube.core.model.collections.CollectionsPartsReference

fun mapCollectionsObjectToCollectionsPartObject(collectionsObject: CollectionsObject): CollectionsPartsObject {
    return CollectionsPartsObject(
        partsReference = CollectionsPartsReference(
            priRef = collectionsObject.priRef,
            dateStart = collectionsObject.datingList,
            recordType = collectionsObject.recordTypeList,
            workTypeList = collectionsObject.workTypeList,
            partsList = collectionsObject.partsList,
            titleList = collectionsObject.titleList,
            formatList = collectionsObject.formatList
        )
    )
}