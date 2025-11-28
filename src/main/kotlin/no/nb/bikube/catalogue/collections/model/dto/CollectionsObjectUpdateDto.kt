package no.nb.bikube.catalogue.collections.model.dto

import kotlinx.serialization.*
import no.nb.bikube.catalogue.collections.config.CollectionsLrefConfig
import no.nb.bikube.core.util.DateUtils
import java.time.LocalDate
import java.time.LocalTime

// ExperimentalSerializationApi and @EncodeDefault are used to avoid serializing null values.
// When they are not present, they will not be updated in Collections.
// If they were null, the value in Collections would be 'null'.

@Serializable
class CollectionsObjectUpdateDto @OptIn(ExperimentalSerializationApi::class) constructor(
    @SerialName("priref")
    val priRef: String,

    @SerialName("notes")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val notes: String? = null,

    @SerialName("Alternative_number")
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val alternativeNumbers: List<AlternativeNumberInput>? = null,

    @SerialName("edit.name")
    val editName: String?,

    @SerialName("edit.date")
    val editDate: String?,

    @SerialName("edit.time")
    val editTime: String?
)

fun createUpdateManifestationDto(
    lrefConfig: CollectionsLrefConfig,
    id: String,
    username: String,
    notes: String?,
    number: String?
): CollectionsObjectUpdateDto {
    val altNumbers = number?.let { listOf(AlternativeNumberInput(it, lrefConfig.avisnr)) }

    return CollectionsObjectUpdateDto(
        priRef = id,
        notes = notes,
        alternativeNumbers = altNumbers,
        editName = username,
        editDate = DateUtils.createDateString(LocalDate.now()),
        editTime = LocalTime.now().toString()
    )
}

