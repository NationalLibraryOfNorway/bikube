package no.nb.bikube.core.model

import com.fasterxml.jackson.annotation.JsonProperty
import no.nb.bikube.core.enum.AxiellDescriptionType

data class CollectionsModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsRecordList
)

data class CollectionsRecordList(
    val recordList: List<CollectionsObject>?
)

data class CollectionsObject(
    @JsonProperty("@priref")
    val priRef: String,

    @JsonProperty("Title")
    val titleList: List<CollectionsTitle>?,

    @JsonProperty("record_type")
    val recordTypeList: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("format")
    val formatList: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("Part_of")
    val partOfList: List<CollectionsPartOfObject>?,

    @JsonProperty("Submedium")
    val subMediumList: List<SubMedium>?,

    @JsonProperty("Medium")
    val mediumList: List<Medium>?,

    @JsonProperty("Dating")
    val datingList: List<CollectionsDating>?,

    @JsonProperty("publisher")
    val publisherList: List<String>?,

    @JsonProperty("Language")
    val languageList: List<CollectionsLanguage>?,

    @JsonProperty("place_of_publication")
    val placeOfPublicationList: List<String>?,

    @JsonProperty("Parts")
    val partsList: List<CollectionsPartsObject>?,

    @JsonProperty("work.description_type")
    val workTypeList: List<List<CollectionsLanguageListObject>>?
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

    @JsonProperty("record_type")
    val recordType: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("group:Submedium")
    val subMedium: List<SubMedium>?,

    @JsonProperty("work.description_type")
    val workTypeList: List<List<CollectionsLanguageListObject>>?
)

data class SubMedium(
    @JsonProperty("submedium")
    val subMedium: String?
)

data class Medium(
    val medium: String?
)

data class CollectionsDating(
    @JsonProperty("dating.date.start")
    val dateFrom: String?,

    @JsonProperty("dating.date.end")
    val dateTo: String?
)

data class CollectionsLanguage(
    val language: String?
)

data class CollectionsPartsObject(
    @JsonProperty("parts_reference")
    val partsReference: CollectionsPartsReference?
)

data class CollectionsPartsReference(
    @JsonProperty("priref")
    val priRef: String?,

    @JsonProperty("group:Title")
    val titleList: List<CollectionsTitle>?,

    @JsonProperty("record_type")
    val recordType: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("work.description_type")
    val workTypeList: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("format")
    val formatList: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("group:Parts")
    val partsList: List<CollectionsPartsObject>?
)

fun CollectionsObject.isSerial(): Boolean {
    return this.workTypeList?.first()?.first()?.text == AxiellDescriptionType.SERIAL.value
}
