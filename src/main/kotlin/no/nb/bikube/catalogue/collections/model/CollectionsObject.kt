package no.nb.bikube.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsModel(
    @JsonProperty("adlibJSON")
    override val adlibJson: CollectionsRecordList
) : CollectionsGenericModel<CollectionsObject>

data class CollectionsRecordList(
    override val recordList: List<CollectionsObject>?,

    val diagnostic: CollectionDiagnostic? = null
) : CollectionsGenericRecordList<CollectionsObject>

data class CollectionsObject(
    @JsonProperty("@priref")
    override val priRef: String,

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

    @JsonProperty("Publisher")
    val publisherList: List<CollectionsPublisher>?,

    @JsonProperty("Language")
    val languageList: List<CollectionsLanguage>?,

    @JsonProperty("AssociationGeo")
    val placeOfPublicationList: List<CollectionsAssociationGeo>?,

    @JsonProperty("Parts")
    val partsList: List<CollectionsPartsObject>?,

    @JsonProperty("Alternative_number")
    val alternativeNumberList: List<CollectionsAlternativeNumber>?,

    @JsonProperty("PID_data_URN")
    val urn: List<String>? = null,

    @JsonProperty("input.name")
    val inputName: List<String>? = null,

    @JsonProperty("input.date")
    val inputDate: List<String>? = null,

    @JsonProperty("input.time")
    val inputTime: List<String>? = null,

    @JsonProperty("input.notes")
    val inputNotes: List<String>? = null,

    @JsonProperty("edit.name")
    val modifiedName: String? = null,

    @JsonProperty("edit.date")
    val modifiedDate: String? = null,

    @JsonProperty("edit.time")
    val modifiedTime: String? = null,

    @JsonProperty("edit.notes")
    val modifiedNotes: String? = null,

    @JsonProperty("current_location.barcode")
    val locationBarcode: String? = null,

    @JsonProperty("notes")
    val notes: List<String>? = emptyList()
) : CollectionsGenericObject

data class CollectionsTitle(
    val title: String?,

    @JsonProperty("title.type")
    val titleType: String? = null
)

data class CollectionsPublisher(
    @JsonProperty("publisher")
    val name: String?
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

    @JsonProperty("group:Dating")
    val datingList: List<CollectionsDating>?,
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

data class CollectionsAssociationGeo(
    @JsonProperty("association.geographical_keyword")
    val name: String?
)

data class CollectionsPartsReference(
    @JsonProperty("priref")
    val priRef: String,

    @JsonProperty("group:Dating")
    val dateStart: List<CollectionsDating>?,

    @JsonProperty("group:Title")
    val titleList: List<CollectionsTitle>?,

    @JsonProperty("record_type")
    val recordType: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("format")
    val formatList: List<List<CollectionsLanguageListObject>>?,

    @JsonProperty("group:Parts")
    val partsList: List<CollectionsPartsObject>?,

    @JsonProperty("PID_data_URN")
    val urn: List<String>? = null
)

data class CollectionsAlternativeNumber(
    @JsonProperty("alternative_number.type")
    val type: String?,

    @JsonProperty("alternative_number")
    val value: String?
)

data class CollectionDiagnostic(
    val error: ErrorMessage?,

    val hits: String?,

    val search: String?,

    val dbname: String?
)

data class ErrorMessage(
    val message: String?
)
