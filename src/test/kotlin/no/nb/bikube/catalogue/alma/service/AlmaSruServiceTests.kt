package no.nb.bikube.catalogue.alma.service

import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.*
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

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AlmaSruServiceTests(
    @Autowired private val almaSruService: AlmaSruService,
    @Autowired private val marcXChangeService: MarcXChangeService
) {

    companion object {
        @JvmStatic
        val mockBackEnd = MockWebServer()

        @JvmStatic
        @DynamicPropertySource
        fun properties(r: DynamicPropertyRegistry) {
            println("PORT: " + mockBackEnd.port.toString())
            r.add("alma.alma-sru-url") { "http://localhost:" + mockBackEnd.port.toString() }
        }

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            try {
                mockBackEnd.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            mockBackEnd.shutdown()
        }
    }


    @BeforeEach
    fun beforeEach() {
        println("MOCKBACKEND")
        println(mockBackEnd.requestCount)
        println(mockBackEnd.hostName)
        println(mockBackEnd.port)
        println(mockBackEnd.url(""))
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

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(sruResponse)
                .addHeader("Content-type", "application/xml")
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

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(sruResponse)
                .addHeader("Content-type", "application/xml")
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
