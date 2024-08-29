package no.nb.bikube.core.model.inputDto

data class ItemUpdateDto(
    val manifestationId: String,
    val username: String,
    val notes: String? = null,
    val number: String? = null,
)
