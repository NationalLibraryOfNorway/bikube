package no.nb.bikube.api.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsTermModel(
    @JsonProperty("adlibJSON")
    override val adlibJson: CollectionsTermRecordList
) : CollectionsGenericModel<CollectionsTermObject>

data class CollectionsTermRecordList(
    override val recordList: List<CollectionsTermObject>?
) : CollectionsGenericRecordList<CollectionsTermObject>

data class CollectionsTermObject(
    @JsonProperty("@priref")
    override val priRef: String,

    @JsonProperty("term")
    val term: String,
) : CollectionsGenericObject
