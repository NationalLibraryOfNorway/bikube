package no.nb.bikube.core.model.inputDto

import java.time.LocalDate

data class ItemUpdateDto(
    val manifestationId: String,
    val username: String,
    val date: LocalDate? = null,
    val notes: String? = null,
    val number: String? = null,
)
