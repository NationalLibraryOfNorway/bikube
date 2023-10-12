package no.nb.bikube.core.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsNameModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsNameRecordList
)

data class CollectionsNameRecordList(
    val recordList: List<CollectionsNameObject>?
)

data class CollectionsNameObject(
    @JsonProperty("priref")
    val priRef: String? = null,

    val name: String,

    @JsonProperty("name.type")
    val nameType: String? = null,

    @JsonProperty("input.date")
    val inputDate: String? = null,

    @JsonProperty("input.time")
    val inputTime: String? = null,

    @JsonProperty("input.source")
    val inputSource: String? = null,

    @JsonProperty("input.name")
    val inputName: String? = null
)