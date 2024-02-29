package no.nb.bikube.core.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
class SecurityConfig {

    @ConditionalOnProperty(
        prefix = "security",
        name = ["enabled"],
        havingValue = "true"
    )
    @Configuration
    @EnableWebFluxSecurity
    class Enabled {
        @Bean
        fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
            http
                .csrf { csrf -> csrf.disable() }
                .authorizeExchange { exchange ->
                    exchange
                        .pathMatchers(
                            "/",
                            "/webjars/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/swagger-resources",
                            "/swagger-resources/**",
                            "/actuator",
                            "/actuator/**"
                        ).permitAll()
                        .pathMatchers(HttpMethod.GET, "/**").permitAll()
                        .anyExchange().hasAuthority("bikube-create")
                }
                .oauth2ResourceServer { server ->
                    server.jwt { jwt ->
                        jwt.jwtAuthenticationConverter(authoritiesExtractor())
                    }
                }
            return http.build()
        }

        fun authoritiesExtractor(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
            val jwtAuthConverter = JwtAuthenticationConverter()
            jwtAuthConverter.setJwtGrantedAuthoritiesConverter(GrantedAuthoritiesExtractor())
            return ReactiveJwtAuthenticationConverterAdapter(jwtAuthConverter)
        }

        internal class GrantedAuthoritiesExtractor : Converter<Jwt, Collection<GrantedAuthority>> {
            override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
                @Suppress("UNCHECKED_CAST")
                val realmAccess: Map<String, List<String>> = jwt.claims.getOrDefault("realm_access", emptyMap<String, List<String>>()) as Map<String, List<String>>
                val roles: List<String> = realmAccess.getOrDefault("roles", emptyList())

                return roles.map { SimpleGrantedAuthority(it) }
            }
        }
    }

    @ConditionalOnProperty(
        prefix = "security",
        name = ["enabled"],
        havingValue = "false"
    )
    @Configuration
    @EnableWebFluxSecurity
    class Disabled {
        @Bean
        fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
            http
                .csrf { csrf -> csrf.disable() }
                .authorizeExchange { authorizeExchange ->
                    authorizeExchange
                        .anyExchange()
                        .permitAll()
                }
            return http.build()
        }
    }
}
