package no.nb.bikube.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsLocationModel(
    @JsonProperty("adlibJSON")
    val adlibJson: CollectionsLocationRecordList?
)

data class CollectionsLocationRecordList(
    val recordList: List<CollectionsLocationObject>?
)

data class CollectionsLocationObject(
    @JsonProperty("@priref")
    val priRef: String?,

    @JsonProperty("name")
    val name: List<String>?,

    @JsonProperty("barcode")
    val barcode: String?,

    // This one is weird for containers/location, use language=0! There we have "container" or "location". Otherwise, it's "package" or "location"
    @JsonProperty("package_location")
    val packageLocation: List<List<CollectionsLanguageListObject>>?
)
