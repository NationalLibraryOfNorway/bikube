package no.nb.bikube.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.resource.PathResourceResolver
import reactor.core.publisher.Mono

@Configuration
class SpaRoutingConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/hugin/**")
            .addResourceLocations("classpath:static/hugin/")
            .resourceChain(true)
            .addResolver(object : PathResourceResolver() {
                override fun getResource(resourcePath: String, location: Resource): Mono<Resource> =
                    super.getResource(resourcePath, location)
                        .switchIfEmpty(Mono.fromCallable { location.createRelative("index.html") })
            })
    }
}
