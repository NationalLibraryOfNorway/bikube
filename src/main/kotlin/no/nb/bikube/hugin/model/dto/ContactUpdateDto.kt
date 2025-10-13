package no.nb.bikube.hugin.model.dto

data class ContactUpdateDto(
    val id: Int,
    val vendor: String? = null,
    val contactName: String? = null,
    val shelf: String? = null,
    val notes: String? = null,
    val contactInfos: List<ContactInfoDto>? = null
)
