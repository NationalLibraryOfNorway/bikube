package no.nb.bikube.api.catalogue.collections.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nb.bikube.api.catalogue.collections.config.CollectionsLrefConfig
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.enum.CollectionsRecordType
import no.nb.bikube.api.core.model.inputDto.TitleInputDto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
class TitleDto(
    @SerialName("priref")
    val priRef: String,

    @SerialName("object_number")
    val objectNumber: String,

    @SerialName("Title")
    val titles: List<CollectionsTitleDto>?,

    @SerialName("record_type")
    val recordType: String?,

    @SerialName("medium.lref")
    val mediumLref: String? = null,

    @SerialName("submedium.lref")
    val subMediumLref: String? = null,

    @SerialName("dating.date.start")
    val dateStart: String? = null,

    @SerialName("dating.date.end")
    val dateEnd: String? = null,

    @SerialName("publisher")
    val publisher: String? = null,

    @SerialName("association.geographical_keyword")
    val placeOfPublication: String? = null,

    @SerialName("language")
    val language: String? = null,

    @SerialName("input.name")
    val inputName: String? = null,

    @SerialName("input.source")
    val inputSource: String? = null,

    @SerialName("input.date")
    val inputDate: String? = null,

    @SerialName("input.time")
    val inputTime: String? = null,

    @SerialName("input.notes")
    val inputNotes: String? = null,

    @SerialName("dataset_name")
    val dataset: String? = null
)

@Serializable
class CollectionsTitleDto(
    val title: String?,

    @SerialName("title.type.lref")
    val titleTypeLref: String? = null,

    @SerialName("title.notes")
    val titleNotes: String? = null
)

fun createTitleDto(lrefConfig: CollectionsLrefConfig, id: String, objectNumber: String, title: TitleInputDto, database: CollectionsDatabase): TitleDto {
    return TitleDto(
        priRef = id, // TODO: After migration this will be generated automatically by collections itself
        objectNumber = objectNumber, // TODO: After migration this will be generated automatically by collections itself
        titles = listOf(CollectionsTitleDto(title.name, lrefConfig.originalTittel)),
        dateStart = title.startDate?.toString(),
        dateEnd = title.endDate?.toString(),
        publisher = title.publisher,
        placeOfPublication = title.publisherPlace,
        language = title.language,
        recordType = CollectionsRecordType.WORK.value,
        mediumLref = lrefConfig.mediumTekst,
        subMediumLref = lrefConfig.submediumAviser,
        inputName = title.username,
        inputNotes = "Registrert i Bikube API",
        inputSource = database.value,
        inputDate = LocalDate.now().toString(),
        inputTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")).toString(),
        dataset = database.value
    )
}
