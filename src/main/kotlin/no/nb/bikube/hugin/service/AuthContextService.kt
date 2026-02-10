package no.nb.bikube.hugin.service

import com.vaadin.hilla.BrowserCallable
import jakarta.annotation.security.PermitAll
import jakarta.servlet.http.HttpServletRequest
import no.nb.bikube.hugin.model.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.lang.NonNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ResponseStatusException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@BrowserCallable
class AuthContextService(
    private val request: HttpServletRequest,
    @Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private val issuerUri: String,
    @Value("\${spring.security.oauth2.client.registration.keycloak-hugin.client-id}")
    private val huginClientId: String
) {

    @PermitAll
    @NonNull
    fun getUserInfo(): @org.jspecify.annotations.NonNull User {
        val context = SecurityContextHolder.getContext()
        val principal = context.authentication?.principal

        if (principal is OidcUser) {
            val roles: List<String> =
                context.authentication?.authorities?.stream()?.map<String> { obj: GrantedAuthority -> obj.authority }
                    ?.toList() ?: emptyList()
            return User(
                username = principal.preferredUsername,
                firstName = principal.givenName,
                lastName = principal.familyName,
                email = principal.email,
                roles = roles
            )
        }
        else {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated")
        }
    }

    @PermitAll
    fun logout(): String {
        val context = SecurityContextHolder.getContext()
        val principal = context.authentication?.principal

        // Get the ID token from the OIDC user
        val idToken = if (principal is OidcUser) {
            principal.idToken?.tokenValue
        } else null

        // Invalidate the session
        request.session?.invalidate()
        SecurityContextHolder.clearContext()

        // Build the post-logout redirect URI
        val scheme = request.scheme
        val serverName = request.serverName
        val serverPort = request.serverPort
        val contextPath = request.contextPath
        val postLogoutRedirectUri = if (serverPort == 80 || serverPort == 443) {
            "$scheme://$serverName$contextPath/"
        } else {
            "$scheme://$serverName:$serverPort$contextPath/"
        }

        // Return the Keycloak logout URL with id_token_hint
        val encodedRedirectUri = URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8)
        return if (idToken != null) {
            "$issuerUri/protocol/openid-connect/logout?client_id=$huginClientId&id_token_hint=$idToken&post_logout_redirect_uri=$encodedRedirectUri"
        } else {
            "$issuerUri/protocol/openid-connect/logout?client_id=$huginClientId&post_logout_redirect_uri=$encodedRedirectUri"
        }
    }
}
