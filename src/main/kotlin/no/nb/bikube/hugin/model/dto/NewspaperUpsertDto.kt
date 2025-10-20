package no.nb.bikube.hugin.model.dto

import java.time.LocalDate

data class NewspaperUpsertDto(
    val titleId: Int?? = null,
    val boxId: String,
    val date: LocalDate,
    val edition: String? = null,
    val received: Boolean = false,
    val notes: String? = null,
    var catalogId: String? = null,
)
