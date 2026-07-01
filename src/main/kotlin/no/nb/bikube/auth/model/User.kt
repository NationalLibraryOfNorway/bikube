package no.nb.bikube.auth.model

data class User(
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val roles: List<String>,
)
