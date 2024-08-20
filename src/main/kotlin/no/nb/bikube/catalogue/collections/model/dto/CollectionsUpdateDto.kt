package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.*
import no.nb.bikube.core.util.DateUtils
import java.time.LocalDate
import java.time.LocalTime

@Serializable
class CollectionsUpdateDto @OptIn(ExperimentalSerializationApi::class) constructor(
    @SerialName("priref")
    val priRef: String,

    @SerialName("notes")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val notes: String? = null,

    @SerialName("dating.date.start")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val date: String? = null,

    @SerialName("production.notes")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val number: String? = null,

    @SerialName("edit.name")
    val editName: String?,

    @SerialName("edit.date")
    val editDate: String?,

    @SerialName("edit.time")
    val editTime: String?
)

fun createUpdateManifestationDto(
    id: String,
    username: String,
    notes: String?,
    date: LocalDate?,
    number: String?
): CollectionsUpdateDto {
    return CollectionsUpdateDto(
        priRef = id,
        notes = notes,
        date = date?.let { DateUtils.createDateString(date) },
        number = number,
        editName = username,
        editDate = DateUtils.createDateString(LocalDate.now()),
        editTime = LocalTime.now().toString()
    )
}

