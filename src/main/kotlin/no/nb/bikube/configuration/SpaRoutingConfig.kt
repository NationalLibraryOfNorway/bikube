package no.nb.bikube.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.resource.PathResourceResolver
import org.springframework.web.reactive.resource.ResourceResolverChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class SpaRoutingConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/hugin", "/hugin/", "/hugin/**")
            .addResourceLocations("classpath:static/hugin/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun resolveResource(
                    exchange: ServerWebExchange?,
                    requestPath: String,
                    locations: List<Resource>,
                    chain: ResourceResolverChain,
                ): Mono<Resource> {
                    val path = requestPath.trimEnd('/')
                    val effectivePath = path.ifEmpty { "index.html" }
                    return super.resolveResource(exchange, effectivePath, locations, chain)
                        .switchIfEmpty(super.resolveResource(exchange, "index.html", locations, chain))
                }
            })
    }
}
