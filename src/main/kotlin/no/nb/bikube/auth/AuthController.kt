package no.nb.bikube.auth

import no.nb.bikube.auth.model.User
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AuthController {

    @GetMapping("/api/auth/me")
    suspend fun getUserInfo(authentication: Authentication): User {
        val principal = authentication.principal as? OidcUser
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return User(
            username = principal.preferredUsername ?: "",
            firstName = principal.givenName,
            lastName = principal.familyName,
            email = principal.email,
            roles = authentication.authorities.map { it.authority },
        )
    }
}
