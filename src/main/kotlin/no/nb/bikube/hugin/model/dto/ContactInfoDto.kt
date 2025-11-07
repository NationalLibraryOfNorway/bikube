package no.nb.bikube.hugin.model.dto

import no.nb.bikube.hugin.model.ContactType

data class ContactInfoDto(
    val contactType: ContactType,
    val contactValue: String
)
