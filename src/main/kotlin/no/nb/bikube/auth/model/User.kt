package no.nb.bikube.auth.model

data class User(
    val username: String,
    // Sourced from OIDC given_name/family_name/email claims, which only appear if the
    // client requests the profile/email scopes (we currently only request openid) and the
    // provider actually populates them. Not a reliable signal of a "legitimate" user.
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val roles: List<String>,
)
