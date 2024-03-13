package no.nb.bikube.catalogue.alma.controller

import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.repository.AlmaRepository
import no.nb.bikube.catalogue.alma.util.DocumentMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.util.StreamUtils
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AlmaControllerTests(
    @Autowired private var webClient: WebTestClient,
    @Autowired private val almaRepository: AlmaRepository,
    @Autowired private val documentMapper: DocumentMapper
) {

    companion object {
        @JvmStatic
        val mockBackEnd = MockWebServer()

        @JvmStatic
        @DynamicPropertySource
        fun properties(r: DynamicPropertyRegistry) {
            r.add("alma.almaws-url") { "http://localhost:" + mockBackEnd.port }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            mockBackEnd.shutdown()
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "9912",  // Too short
            "99122564260470220222",  // Too long
            "abbaabbaabbaabba" // Invalid characters
        ]
    )
    fun shouldValidateMMS(mms: String) {
        webClient.get().uri("/alma/mms/$mms")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ProblemDetail::class.java)
            .consumeWith { problemDetail ->
                Assertions.assertEquals(
                    problemDetail.responseBody!!.detail,
                    "getAlmaItemByMMS.mms: ${AlmaController.MMS_MESSAGE}"
                )
            }
    }

    @Test
    fun `Alma response should be mapped to expected Marc record`() {
        val bibResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_result.xml").inputStream,
            Charsets.UTF_8
        )
        val marcRecord = StreamUtils.copyToByteArray(
            ClassPathResource("AlmaXmlTestFiles/bib_marc.xml").inputStream
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(bibResponse)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402", prolog = false)

        StepVerifier.create(almaResponse)
            .expectNextMatches { documentMapper.parseDocument(it) == documentMapper.parseDocument(marcRecord) }
            .verifyComplete()
    }

    @Test
    fun `Alma error code 402203 should be mapped to an AlmaRecordNotFoundException`() {
        val bibError = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_error_not_found.xml").inputStream,
            Charsets.UTF_8
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(bibError)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402", prolog = false)

        StepVerifier.create(almaResponse)
            .expectErrorMatches {
                it is AlmaRecordNotFoundException &&
                        it.message == "Input parameters mmsId 987654321098765402 is not valid."
            }
            .verify()
    }

    @Test
    fun `Alma error code 402204 should be mapped to an IllegalArgumentException`() {
        val bibError = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_error_invalid.xml").inputStream,
            Charsets.UTF_8
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(bibError)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "ABCDEF12345", prolog = false)

        StepVerifier.create(almaResponse)
            .expectErrorMatches {
                it is IllegalArgumentException &&
                        it.message == "Input parameters mmsid ABCDEF12345 is not numeric."
            }
            .verify()
    }

    @Test
    fun `Alma error code 401652 should be mapped to an AlmaException`() {
        val bibError = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_error_general_error.xml").inputStream,
            Charsets.UTF_8
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(bibError)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402", prolog = false)

        StepVerifier.create(almaResponse)
            .expectErrorMatches {
                it is AlmaException &&
                        it.message == "[General Error - An error has occurred while processing the request.]"
            }
            .verify()
    }

    @Test
    fun `A 500 error from AlmaWs should be mapped to an AlmaException`() {
        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("Server error")
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402", prolog = false)

        StepVerifier.create(almaResponse)
            .expectErrorMatches { it is AlmaException && it.message == "Server error" }
            .verify()
    }

}
