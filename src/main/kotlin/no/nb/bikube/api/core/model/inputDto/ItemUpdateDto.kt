package no.nb.bikube.api.core.model.inputDto

data class ItemUpdateDto(
    val manifestationId: String,
    val username: String,
    val notes: String? = null,
    val number: String? = null,
)
