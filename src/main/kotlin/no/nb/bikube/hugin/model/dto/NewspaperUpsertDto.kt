package no.nb.bikube.hugin.model.dto

import java.time.LocalDate

data class NewspaperUpsertDto(
    val titleId: Int,
    val boxId: String,
    val date: LocalDate?,
    val edition: String?,
    val received: Boolean,
    val notes: String?,
    var catalogId: String? = null,
)
