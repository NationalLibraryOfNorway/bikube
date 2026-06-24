package no.nb.bikube.api.catalogue.collections.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nb.bikube.api.core.util.DateUtils.Companion.parseYearOrDate
import java.time.LocalDate

data class CollectionsSeriesModel(
    @JsonProperty("adlibJSON")
    override val adlibJson: CollectionsSeriesRecordList
) : CollectionsGenericModel<CollectionsSeriesObject>

data class CollectionsSeriesRecordList(
    override val recordList: List<CollectionsSeriesObject>?,
    val diagnostic: CollectionDiagnostic? = null
) : CollectionsGenericRecordList<CollectionsSeriesObject>

@JsonIgnoreProperties(ignoreUnknown = true)
data class CollectionsSeriesObject(
    @JsonProperty("@priref")
    override val priRef: String,

    @JsonProperty("series")
    val seriesTitles: List<String>?,

    @JsonProperty("Dating")
    val datingList: List<CollectionsSeriesDating>?,

    @JsonProperty("publisher")
    val publisher: String? = null,

    @JsonProperty("place_of_publication")
    val placeOfPublication: String? = null,

    @JsonProperty("language")
    val language: String? = null,
) : CollectionsGenericObject

data class CollectionsSeriesDating(
    @JsonProperty("date_start")
    val dateFrom: String?,

    @JsonProperty("date_end")
    val dateTo: String?
)

fun CollectionsSeriesModel.getError(): String? = this.adlibJson.diagnostic?.error?.message

fun CollectionsSeriesModel.hasError(): Boolean = this.getError() != null

fun CollectionsSeriesObject.getName(): String? = this.seriesTitles?.firstOrNull()

fun CollectionsSeriesObject.getStartDate(): LocalDate? =
    this.datingList?.firstOrNull()?.dateFrom?.let { parseYearOrDate(it) }

fun CollectionsSeriesObject.getEndDate(): LocalDate? =
    this.datingList?.firstOrNull()?.dateTo?.let { parseYearOrDate(it) }
