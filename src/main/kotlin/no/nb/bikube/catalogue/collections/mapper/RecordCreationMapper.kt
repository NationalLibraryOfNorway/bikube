package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.model.CollectionsObject
import no.nb.bikube.catalogue.collections.model.CollectionsPartsObject
import no.nb.bikube.catalogue.collections.model.CollectionsPartsReference

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