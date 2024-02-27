package no.nb.bikube.catalog.collections.mapper

import no.nb.bikube.catalog.collections.model.CollectionsObject
import no.nb.bikube.catalog.collections.model.CollectionsPartsObject
import no.nb.bikube.catalog.collections.model.CollectionsPartsReference

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