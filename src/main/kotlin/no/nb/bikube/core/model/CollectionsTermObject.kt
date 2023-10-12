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
    @JsonProperty("priref")
    val priRef: String? = null,

    @JsonProperty("term")
    val term: String,

    @JsonProperty("term.type")
    val termType: String? = null,

    @JsonProperty("input.date")
    val inputDate: String? = null,

    @JsonProperty("input.time")
    val inputTime: String? = null,

    @JsonProperty("input.source")
    val inputSource: String? = null,

    @JsonProperty("input.name")
    val inputName: String? = null
)