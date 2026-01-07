package no.nb.bikube.api.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsLocationModel(
    @JsonProperty("adlibJSON")
    override val adlibJson: CollectionsLocationRecordList
) : CollectionsGenericModel<CollectionsLocationObject>

data class CollectionsLocationRecordList(
    override val recordList: List<CollectionsLocationObject>?
) : CollectionsGenericRecordList<CollectionsLocationObject>

data class CollectionsLocationObject(
    @JsonProperty("@priref")
    override val priRef: String,

    @JsonProperty("name")
    val name: List<String>?,

    @JsonProperty("barcode")
    val barcode: String?,

    // This one is weird for containers/location, use language=0! There we have "container" or "location". Otherwise, it's "package" or "location"
    @JsonProperty("package_location")
    val packageLocation: List<List<CollectionsLanguageListObject>>?
) : CollectionsGenericObject
