package no.nb.bikube.api.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonProperty

data class CollectionsNameModel(
    @JsonProperty("adlibJSON")
    override val adlibJson: CollectionsNameRecordList
) : CollectionsGenericModel<CollectionsNameObject>

data class CollectionsNameRecordList(
    override val recordList: List<CollectionsNameObject>?
) : CollectionsGenericRecordList<CollectionsNameObject>

data class CollectionsNameObject(
    @JsonProperty("@priref")
    override val priRef: String,

    @JsonProperty("name")
    val name: String,
) : CollectionsGenericObject
