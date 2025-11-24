package no.nb.bikube.catalogue.collections.model

import no.nb.bikube.catalogue.collections.exception.CollectionsObjectMissing

interface CollectionsGenericObject {
    val priRef: String
}

interface CollectionsGenericRecordList<T> {
    val recordList: List<T>?
}

interface CollectionsGenericModel<T> {
    val adlibJson: CollectionsGenericRecordList<T>

    fun getObjects(): List<T>? {
        return this.adlibJson.recordList
    }

    fun hasObjects(): Boolean {
        return this.getObjects()?.isNotEmpty() ?: false
    }

    @Throws(CollectionsObjectMissing::class)
    fun getFirstObject(): T {
        return this.getObjects().takeIf { !it.isNullOrEmpty() }
            ?. first()
            ?: throw CollectionsObjectMissing()
    }
}
