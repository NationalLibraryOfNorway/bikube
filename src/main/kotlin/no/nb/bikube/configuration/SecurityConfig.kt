package no.nb.bikube.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

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
                        "/hugin/assets/**", "/hugin/index.html", "/favicon.ico",
                        "/actuator/**"
                    ).permitAll()
                    .anyExchange().authenticated()
            }
            .oauth2Login { }
            .oauth2ResourceServer { it.jwt { } }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    Mono.empty()
                }
            }
            .logout { it.logoutSuccessHandler(oidcLogoutSuccessHandler()) }
            .csrf { it.disable() }
            .build()

    private fun oidcLogoutSuccessHandler() =
        OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository)
            .also { it.setPostLogoutRedirectUri("{baseUrl}/bikube/hugin/") }
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
