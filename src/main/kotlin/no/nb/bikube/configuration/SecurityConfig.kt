package no.nb.bikube.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.DefaultServerRedirectStrategy
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache
import reactor.core.publisher.Mono
import java.net.URI

fun realmRoleAuthoritiesConverter(): Converter<Jwt, Collection<GrantedAuthority>> =
    Converter { jwt ->
        val realmAccess = (jwt.claims["realm_access"] as? Map<*, *>) ?: emptyMap<String, Any>()
        val roles = (realmAccess["roles"] as? Collection<*>)?.filterIsInstance<String>() ?: emptyList()
        roles.map { SimpleGrantedAuthority(it) }
    }

// Interactive hugin logins carry DIMO group membership in the "groups" claim (as opposed to
// the realm_access.roles claim used for Bearer-token/M2M auth). Picked up automatically by
// ServerHttpSecurity's oauth2Login default authentication manager (OAuth2LoginSpec.createDefault()
// looks up a GrantedAuthoritiesMapper bean), same as the pre-migration servlet config's
// userAuthoritiesMapper().
fun groupsAuthoritiesMapper(): GrantedAuthoritiesMapper =
    GrantedAuthoritiesMapper { authorities ->
        authorities.filterIsInstance<OidcUserAuthority>()
            .flatMap { authority ->
                val roles = authority.userInfo.getClaim<List<String>>("groups") ?: emptyList()
                roles.flatMap { role -> listOf(SimpleGrantedAuthority(role), SimpleGrantedAuthority("ROLE_$role")) }
            }
    }

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "true")
class SecurityConfig(
    private val clientRegistrationRepository: ReactiveClientRegistrationRepository,
) {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .authorizeExchange { auth ->
                auth
                    .pathMatchers(
                        "/oauth2/**", "/login/**", "/logout",
                        "/hugin/assets/**", "/favicon.ico",
                        "/actuator/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                    ).permitAll()
                    .pathMatchers("/api/hugin/**").authenticated()
                    .pathMatchers(HttpMethod.GET, "/api/**").permitAll()
                    .pathMatchers("/api/**").hasAuthority("bikube-create")
                    .anyExchange().authenticated()
            }
            .oauth2Login { oauth2 ->
                oauth2.authenticationSuccessHandler(savedRequestAwareSuccessHandler())
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    val converter = JwtAuthenticationConverter().apply {
                        setJwtGrantedAuthoritiesConverter(realmRoleAuthoritiesConverter())
                    }
                    jwt.jwtAuthenticationConverter(ReactiveJwtAuthenticationConverterAdapter(converter))
                }
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { exchange, _ ->
                    val path = exchange.request.path.pathWithinApplication().value()
                    if (path.startsWith("/api/")) {
                        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        Mono.empty()
                    } else {
                        WebSessionServerRequestCache().saveRequest(exchange)
                            .then(DefaultServerRedirectStrategy().sendRedirect(
                                exchange, URI.create("/oauth2/authorization/keycloak-hugin")
                            ))
                    }
                }
            }
            .logout { it.logoutSuccessHandler(oidcLogoutSuccessHandler()) }
            .csrf { it.disable() }
            .build()

    private fun savedRequestAwareSuccessHandler(): ServerAuthenticationSuccessHandler {
        val requestCache = WebSessionServerRequestCache()
        val redirectStrategy = DefaultServerRedirectStrategy()
        return ServerAuthenticationSuccessHandler { exchange, _ ->
            requestCache.getRedirectUri(exchange.exchange)
                .defaultIfEmpty(URI.create("/hugin"))
                .flatMap { uri -> redirectStrategy.sendRedirect(exchange.exchange, uri) }
        }
    }

    private fun oidcLogoutSuccessHandler() =
        OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)
            .also { it.setPostLogoutRedirectUri("{baseUrl}/hugin") }

    @Bean
    fun grantedAuthoritiesMapper(): GrantedAuthoritiesMapper = groupsAuthoritiesMapper()
}

@Configuration
@EnableWebFluxSecurity
@ConditionalOnProperty(name = ["security.enabled"], havingValue = "false", matchIfMissing = true)
class PermissiveSecurityConfig {

    @Bean
    fun permissiveFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .authorizeExchange { it.anyExchange().permitAll() }
            .csrf { it.disable() }
            .build()
}
