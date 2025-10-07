package no.nb.bikube.hugin.model

import org.springframework.lang.NonNull
import kotlin.plus
import kotlin.text.firstOrNull
import kotlin.text.uppercase

data class User (
    @NonNull
    val username: String,
    @NonNull
    val firstName: String,
    @NonNull
    val lastName: String,
    @NonNull
    val email: String,
    @NonNull
    val roles: List<@org.jspecify.annotations.NonNull String>
) {
    public val fullName: String
        get() = "$firstName $lastName"

    val initials: String
        get() = firstName.firstOrNull()?.toString()?.uppercase() + lastName.firstOrNull()?.toString()?.uppercase() ?: ""
}
