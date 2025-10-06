package no.nb.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.PermitAll
import no.nb.hugin.model.User
import org.springframework.lang.NonNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser

@BrowserCallable
class AuthContextService {

    @PermitAll
    @NonNull
    fun getUserInfo(): @org.jspecify.annotations.NonNull User {
        val context = SecurityContextHolder.getContext()
        val principal = context.authentication.principal

        if (principal is OidcUser) {
            val roles: List<String> =
                context.authentication.authorities.stream().map<String> { obj: GrantedAuthority -> obj.authority }.toList()
            return User(
                username = principal.preferredUsername,
                firstName = principal.givenName,
                lastName = principal.familyName,
                email = principal.email,
                roles = roles
            )
        } else throw kotlin.RuntimeException("Not a valid OidcUser")
    }
}
