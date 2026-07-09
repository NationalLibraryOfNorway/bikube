package no.nb.bikube.auth.model

data class User(
    val username: String,
    // Nullable because Spring's OidcUser types these claims as nullable, even though our
    // Keycloak setup always populates them.
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val roles: List<String>,
)
