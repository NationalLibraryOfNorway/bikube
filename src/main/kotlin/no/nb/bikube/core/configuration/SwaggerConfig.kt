package no.nb.bikube.core.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openApi(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Bikube - API for kommuniksjon med tekstkataloger")
                    .description(
                        "REST-API for kommunikasjon mot kataloger for tekstmateriale. " +
                        "Brukes av produksjonsløypene for å lettere kunne hente ut katalog-data enn ved direkteintegrering. "
                    )
            )
    }
}
