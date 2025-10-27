package no.nb.bikube.api.core.configuration

import com.vaadin.flow.spring.security.VaadinWebSecurity
import jakarta.servlet.http.HttpServletRequest
import no.nb.bikube.api.core.util.logger
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.util.AntPathMatcher
import java.util.stream.Collectors
import java.util.stream.Stream

@Configuration
@Order(1)
class ApiSecurityConfig {

    companion object {
        fun apiSecurityMatcher() = listOf(
            "/api/**",
            "/webjars/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources",
            "/swagger-resources/**",
            "/actuator",
            "/actuator/**"
        )
    }

    @ConditionalOnProperty(
        prefix = "security",
        name = ["enabled"],
        havingValue = "true"
    )
    @Configuration
    @EnableWebSecurity
    class Enabled {
        @Bean
        fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http.securityMatcher(*apiSecurityMatcher().toTypedArray())

            val jwtAuthConverter = JwtAuthenticationConverter().apply {
                setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter())
            }

            http
                .csrf { it -> it.disable() }
                .authorizeHttpRequests { auth ->
                    auth
                        .requestMatchers(HttpMethod.GET,*apiSecurityMatcher().toTypedArray()).permitAll()
                        .requestMatchers(*apiSecurityMatcher().toTypedArray()).hasAuthority("bikube-create")
                }
                .oauth2ResourceServer { server ->
                    server.jwt { jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthConverter)
                    }
                }
                .securityMatcher(*apiSecurityMatcher().toTypedArray())
            return http.build()
        }

        @Bean
        fun grantedAuthoritiesConverter(): org.springframework.core.convert.converter.Converter<Jwt, Collection<GrantedAuthority>> =
            org.springframework.core.convert.converter.Converter { jwt ->
                val realmAccess = (jwt.claims["realm_access"] as? Map<*, *>) ?: emptyMap<String, Any>()
                val roles = (realmAccess["roles"] as? Collection<*>)?.filterIsInstance<String>() ?: emptyList()
                roles.map { SimpleGrantedAuthority(it) }
            }

    }

    @ConditionalOnProperty(
        prefix = "security",
        name = ["enabled"],
        havingValue = "false"
    )
    @Configuration
    @EnableWebSecurity
    class Disabled {
        @Bean
        fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
            http
                .securityMatcher(*apiSecurityMatcher().toTypedArray())
                .csrf { csrf -> csrf.disable() }
                .authorizeHttpRequests { auth ->
                    auth
                        .requestMatchers(*apiSecurityMatcher().toTypedArray()).permitAll()
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
    }

    // For logging purposes, we add a filter that logs the filter used and the request URI
    @Bean
    fun vaadinChainLoggingFilter(): jakarta.servlet.Filter {
        val matcher = vaadinSecurityMatcher()
        val pathMatcher = AntPathMatcher()
        return jakarta.servlet.Filter { request, response, chain ->
            if (matcher.any { pattern ->
                    pathMatcher.match(
                        pattern,
                        (request as HttpServletRequest).servletPath
                    )
                }) {
                logger().debug("VaadinSecurityFilterChain handling: ${(request as HttpServletRequest).servletPath}")
            } else {
                logger().debug("VaadinSecurityFilterChain skipping: ${(request as HttpServletRequest).servletPath}")
            }
            chain.doFilter(request, response)
        }
    }

    override fun configure(http: HttpSecurity) {

        super.configure(http)
        setOAuth2LoginPage(http, "/oauth2/authorization/keycloak-hugin")
        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/").permitAll() // Allow root path for redirect controller
            }
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfoEndpoint -> userInfoEndpoint.userAuthoritiesMapper(this.userAuthoritiesMapper()) }
            }
            .csrf { csrf -> csrf.disable() }
            .logout { it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)) }
    }

    @Bean(name = ["VaadinSecurityFilterChainBean"])
    override fun filterChain(http: HttpSecurity): SecurityFilterChain {
        // For logging purposes, we add a filter that logs the filter used and the request URI
        http
            .addFilterBefore(vaadinChainLoggingFilter(), SecurityContextHolderFilter::class.java)
            .securityMatcher(*vaadinSecurityMatcher().toTypedArray())
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
                    Stream.concat(joinedAuthorities, roleAuthorities)
                }
                .collect(Collectors.toList())
        }
    }

}
