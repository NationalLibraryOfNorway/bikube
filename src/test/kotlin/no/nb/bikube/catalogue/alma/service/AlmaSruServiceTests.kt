package no.nb.bikube.catalogue.alma.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.util.StreamUtils
import org.xmlunit.matchers.CompareMatcher
import reactor.test.StepVerifier
import java.net.ServerSocket
import kotlin.properties.Delegates

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AlmaSruServiceTests(
    @Autowired private val almaSruService: AlmaSruService,
    @Autowired private val marcXChangeService: MarcXChangeService
) {

    companion object {

        @JvmStatic
        lateinit var mockBackEnd: WireMockServer

        @JvmStatic
        var port by Delegates.notNull<Int>()

        @JvmStatic
        @BeforeAll
        fun setup() {
            mockBackEnd = WireMockServer(WireMockConfiguration.options().port(port))
            mockBackEnd.start()
            configureFor("localhost", port)
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(r: DynamicPropertyRegistry) {
            val serverSocket = ServerSocket(0)
            port = serverSocket.localPort
            r.add("alma.alma-sru-url") { "http://localhost:$port" }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            mockBackEnd.shutdown()
        }
    }

    @Test
    fun `Alma SRU response should be mapped to correct RecordList`() {
        val sruResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/sru_result.xml").inputStream,
            Charsets.UTF_8
        )
        val recordList = StreamUtils.copyToByteArray(
            ClassPathResource("AlmaXmlTestFiles/marc_issn.xml").inputStream
        )

        stubFor(any(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(sruResponse)
                    .withHeader("Content-type", "application/xml")
            )
        )

        val almaSruResponse = almaSruService.getRecordsByISSN(issn = "12345678")
            .block()!!
        val mappedRecordList = marcXChangeService.writeAsByteArray(almaSruResponse)
        MatcherAssert.assertThat(mappedRecordList, CompareMatcher.isIdenticalTo(recordList).ignoreWhitespace())
    }

    @Test
    fun `An empty SRU response should be mapped to AlmaRecordNotFoundException`() {
        val sruResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/sru_result_empty.xml").inputStream,
            Charsets.UTF_8
        )

        stubFor(any(anyUrl())
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(sruResponse)
                    .withHeader("Content-type", "application/xml")
            )
        )

        val almaSruResponse = almaSruService.getRecordsByISSN(issn = "1234-5678")

        StepVerifier.create(almaSruResponse)
            .expectErrorMatches {
                it is AlmaRecordNotFoundException &&
                        it.message == "No record found for ISSN 1234-5678"
            }
            .verify()
    }
}
