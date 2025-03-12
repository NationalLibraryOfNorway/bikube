package no.nb.bikube.catalogue.alma.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.core.util.logger
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
import org.springframework.util.StreamUtils
import org.xmlunit.matchers.CompareMatcher
import reactor.test.StepVerifier

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AlmaSruServiceTests(
    @Autowired private val almaSruService: AlmaSruService,
    @Autowired private val marcXChangeService: MarcXChangeService
) {

    lateinit var mockBackEnd: WireMockServer

    @BeforeAll
    fun setup() {
        logger().info("Setting up WireMockServer...")
        mockBackEnd = WireMockServer(WireMockConfiguration.options().port(12345))
        logger().info("Starting")
        mockBackEnd.start()
        logger().info("Configuring")
        configureFor("localhost", 12345)
        logger().info("Done!")

        logger().info("Mock: ${mockBackEnd.baseUrl()}, ${mockBackEnd.isRunning}")
    }

    @AfterAll
    fun shutdown() {
        logger().info("Shutting down")
        mockBackEnd.shutdown()
        logger().info("Finished")
    }

    @Test
    fun `Alma SRU response should be mapped to correct RecordList`() {
        logger().info("Running test 1")
        val sruResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/sru_result.xml").inputStream,
            Charsets.UTF_8
        )
        val recordList = StreamUtils.copyToByteArray(
            ClassPathResource("AlmaXmlTestFiles/marc_issn.xml").inputStream
        )

        stubFor(get(urlPathMatching("/47BIBSYS_NB"))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(sruResponse)
                    .withHeader("Content-type", "application/xml")
            )
        )

        val almaSruResponse = almaSruService.getRecordsByISSN(issn = "12345678")
            .block()!!
        logger().info("Received $almaSruResponse")
        val mappedRecordList = marcXChangeService.writeAsByteArray(almaSruResponse)
        MatcherAssert.assertThat(mappedRecordList, CompareMatcher.isIdenticalTo(recordList).ignoreWhitespace())
    }

    @Test
    fun `An empty SRU response should be mapped to AlmaRecordNotFoundException`() {
        logger().info("Running test 2")
        val sruResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/sru_result_empty.xml").inputStream,
            Charsets.UTF_8
        )

        stubFor(get(urlPathMatching("/47BIBSYS_NB"))
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
