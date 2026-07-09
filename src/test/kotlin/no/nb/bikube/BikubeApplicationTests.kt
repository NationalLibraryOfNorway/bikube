package no.nb.bikube

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.io.File

@Import(TestcontainersConfig::class)
@SpringBootTest
@ActiveProfiles("test")
class BikubeApplicationTests {

	@Autowired
	private lateinit var applicationContext: ApplicationContext

	@Test
	fun contextLoads() {
	}

	@Test
	fun `write openapi spec to file`() {
		WebTestClient
			.bindToApplicationContext(applicationContext)
			.build()
			.get()
			.uri("/v3/api-docs")
			.exchange()
			.expectStatus().isOk
			.expectBody(String::class.java)
			.consumeWith { File("openapi.json").writeText(it.responseBody!!) }
	}
}
