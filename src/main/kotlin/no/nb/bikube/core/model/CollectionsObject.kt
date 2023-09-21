package no.nb.bikube.core.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsRecordList
)

data class CollectionsRecordList(
    val recordList: List<CollectionsObject>
)

data class CollectionsObject(
    @JsonProperty("Title")
    val title: List<CollectionsTitle>,

    @JsonProperty("object_number")
    val objectNumber: String,

    @JsonProperty("priref")
    val priRef: String,

    @JsonProperty("record_type")
    val recordType: List<List<RecordType>>?
)

data class CollectionsTitle(
    val title: String?
)

data class RecordType(
    @JsonProperty("@lang")
    val lang: String?,

    @JsonProperty("#text")
    val text: String?
)