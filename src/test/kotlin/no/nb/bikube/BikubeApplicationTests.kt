package no.nb.bikube

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.context.annotation.Import

@Import(TestcontainersConfig::class)
@SpringBootTest
@ActiveProfiles("test")
class BikubeApplicationTests {

	@Test
	fun contextLoads() {
	}

}
