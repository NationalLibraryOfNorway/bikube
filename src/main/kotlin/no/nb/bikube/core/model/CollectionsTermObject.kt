package no.nb.bikube.core.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsTermModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsTermRecordList
)

data class CollectionsTermRecordList(
    val recordList: List<CollectionsTermObject>?
)

data class CollectionsTermObject(
    @JsonProperty("@priref")
    val priRef: String,

    @JsonProperty("term")
    val term: String,
)