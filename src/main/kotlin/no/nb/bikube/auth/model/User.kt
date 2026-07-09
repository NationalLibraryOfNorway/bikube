package no.nb.bikube.auth.model

data class User(
    val username: String,
    // Nullable: only present in the OIDC token if the profile/email scopes are requested.
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val roles: List<String>,
)
