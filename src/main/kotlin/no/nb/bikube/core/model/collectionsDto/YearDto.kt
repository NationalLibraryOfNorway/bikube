package no.nb.bikube.core.model.collectionsDto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.core.enum.CollectionsDescriptionType
import no.nb.bikube.core.enum.CollectionsRecordType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class YearDto(
    @SerialName("part_of_reference.lref")
    val partOfReference: String?,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("work.description_type")
    val descriptionType: String?,

    @SerialName("dating.date.start")
    val dateStart: String? = null,

    @SerialName("title")
    val title: String? = null,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("input.source")
    val inputSource: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("dataset_name")
    val dataset: String? = null
)

fun createYearDto(titleCatalogueId: String, year: String): YearDto {
    return YearDto(
        partOfReference = titleCatalogueId,
        recordType = CollectionsRecordType.WORK.value,
        descriptionType = CollectionsDescriptionType.YEAR.value,
        dateStart = year,
        title = null,
        inputName = "Bikube API", // TODO: Change when we have authentication in place
        inputSource = "texts>texts",
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = "texts"
    )
}
