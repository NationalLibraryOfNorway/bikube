package no.nb.bikube.core.configuration

import com.vaadin.flow.spring.security.VaadinWebSecurity
import jakarta.servlet.http.HttpServletRequest
import no.nb.bikube.core.util.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.util.AntPathMatcher
import reactor.core.publisher.Mono
import java.util.stream.Collectors
import java.util.stream.Stream

@Configuration
@Order(1)
class ApiSecurityConfig {

    @ConditionalOnProperty(
        prefix = "security",
        name = ["enabled"],
        havingValue = "true"
    )
    @Configuration
    @EnableWebFluxSecurity
    class Enabled {
        @Bean
        fun apiSecurityEnabledFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
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
        fun apiSecurityDisabledFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
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

@Order(2)
@EnableWebSecurity
@Configuration
@Profile("!no-vaadin")
class VaadinSecurityConfig : VaadinWebSecurity() {

    companion object {
        fun vaadinSecurityMatcher() = listOf(
            "/",
            "/login/**",
            "/oauth2/**",
            "/hugin/**",
            "/connect/**"
        )

        // For logging purposes, we add a filter that logs the filter used and the request URI
        @Bean
        fun vaadinChainLoggingFilter(): jakarta.servlet.Filter {
            val matcher = vaadinSecurityMatcher()
            val pathMatcher = AntPathMatcher()
            return jakarta.servlet.Filter { request, response, chain ->
                if (matcher.any { pattern -> pathMatcher.match(pattern, (request as HttpServletRequest).servletPath) }) {
                    logger().debug("VaadinSecurityFilterChain handling: ${(request as HttpServletRequest).servletPath}")
                } else {
                    logger().debug("VaadinSecurityFilterChain skipping: ${(request as HttpServletRequest).servletPath}")
                }
                chain.doFilter(request, response)
            }
        }
    }

    override fun configure(http: HttpSecurity) {
        super.configure(http)
        setOAuth2LoginPage(http, "/oauth2/authorization/keycloak")
        http.oauth2Login { oauth2 ->
            oauth2.userInfoEndpoint { userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(this.userAuthoritiesMapper()) }
        }
            // TODO: Ammo feiler med invalid csrf-token
            .csrf { csrf ->
                csrf.disable()
            }
        http.logout {
            it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
        }
    }

    @Bean(name = ["VaadinSecurityFilterChainBean"])
    override fun filterChain(http: HttpSecurity): SecurityFilterChain {
        // For logging purposes, we add a filter that logs the filter used and the request URI
        http.addFilterBefore(vaadinChainLoggingFilter(), SecurityContextHolderFilter::class.java)

        http.securityMatcher(
            *vaadinSecurityMatcher().toTypedArray()
        )
        return super.filterChain(http)
    }

    @Bean
    fun userAuthoritiesMapper(): GrantedAuthoritiesMapper {
        return GrantedAuthoritiesMapper { authorities: Collection<GrantedAuthority?> ->
            authorities.stream()
                .filter { authority: GrantedAuthority? -> OidcUserAuthority::class.java.isInstance(authority) }
                .map { authority: GrantedAuthority? ->
                    val oidcUserAuthority = authority as OidcUserAuthority?
                    val userInfo = oidcUserAuthority!!.userInfo
                    val roles = userInfo.getClaim<List<String>>("groups")
                    roles.stream().map { r: String? -> SimpleGrantedAuthority("ROLE_$r") }
                }
                .reduce(Stream.empty()) { joinedAuthorities: Stream<SimpleGrantedAuthority>, roleAuthorities: Stream<SimpleGrantedAuthority> ->
                    Stream.concat(
                        joinedAuthorities,
                        roleAuthorities
                    )
                }
                .collect(Collectors.toList())
        }
    }

}
