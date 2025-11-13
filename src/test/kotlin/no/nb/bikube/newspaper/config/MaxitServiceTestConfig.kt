package no.nb.bikube.newspaper.config

import io.mockk.every
import io.mockk.mockk
import no.nb.bikube.api.newspaper.model.ParsedIdResponse
import no.nb.bikube.newspaper.service.MaxitService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class MaxitServiceTestConfig {
    @Bean
    @Primary
    fun maxitService(): MaxitService {
        return mockk<MaxitService> {
            every { getUniqueIds() } returns ParsedIdResponse("1000", "2000")
        }
    }
}
