package no.nb.bikube.core.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

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
                .csrf{ csrf -> csrf.disable()}
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
                        .anyExchange().authenticated()
                }
                .httpBasic {  }
            return http.build()
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
                .csrf{ csrf -> csrf.disable()}
                .authorizeExchange { authorizeExchange ->
                    authorizeExchange
                        .anyExchange()
                        .permitAll()
                }
            return http.build()
        }
    }
}