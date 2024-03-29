package no.nb.bikube.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsNameModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsNameRecordList
)

data class CollectionsNameRecordList(
    val recordList: List<CollectionsNameObject>?
)

data class CollectionsNameObject(
    @JsonProperty("@priref")
    val priRef: String?,

    @JsonProperty("name")
    val name: String,
)
