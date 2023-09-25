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
    @JsonProperty("priref")
    val priRef: String,

    @JsonProperty("object_number")
    val objectNumber: String,

    @JsonProperty("Title")
    val title: List<CollectionsTitle>?,

    @JsonProperty("record_type")
    val recordType: List<List<CollectionsLanguageListObject>>?,

    val format: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("Part_of")
    val partOf: List<CollectionsPartOfObject>?,

    @JsonProperty("submedium")
    val subMedium: List<SubMedium>?,

    val medium: List<Medium>?
)

data class CollectionsTitle(
    val title: String?
)

data class CollectionsLanguageListObject(
    @JsonProperty("@lang")
    val lang: String?,

    @JsonProperty("#text")
    val text: String?
)

data class CollectionsPartOfObject(
    @JsonProperty("part_of_reference")
    val partOfReference: CollectionsPartOfReference?
)

data class CollectionsPartOfReference(
    @JsonProperty("priref")
    val priRef: String?,

    @JsonProperty("group:Part_of")
    val partOfGroup: List<CollectionsPartOfObject>?,

    @JsonProperty("group:Title")
    val title: List<CollectionsTitle>?,

    @JsonProperty("object_number")
    val objectNumber: String?,

    @JsonProperty("record_type")
    val recordType: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("group:Submedium")
    val subMedium: List<SubMedium>?,
)

data class SubMedium(
    @JsonProperty("submedium")
    val subMedium: String?
)

data class Medium(
    val medium: String?
)
