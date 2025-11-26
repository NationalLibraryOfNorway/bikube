package no.nb.bikube.api.newspaper.config

import io.mockk.every
import io.mockk.mockk
import no.nb.bikube.api.newspaper.model.ParsedIdResponse
import no.nb.bikube.api.newspaper.service.MaxitService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import reactor.core.publisher.Mono

@TestConfiguration
class MaxitServiceTestConfig {
    @Bean
    @Primary
    fun maxitService(): MaxitService {
        return mockk<MaxitService> {
            every { getUniqueIds() } returns Mono.just(ParsedIdResponse("1000", "2000"))
        }
    }
}
