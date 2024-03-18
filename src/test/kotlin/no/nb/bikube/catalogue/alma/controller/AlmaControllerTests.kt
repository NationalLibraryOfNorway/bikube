package no.nb.bikube.catalogue.alma.controller

import no.nb.bikube.catalogue.alma.exception.AlmaException
import no.nb.bikube.catalogue.alma.exception.AlmaRecordNotFoundException
import no.nb.bikube.catalogue.alma.repository.AlmaRepository
import no.nb.bikube.catalogue.alma.service.MarcXChangeService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.MatcherAssert.assertThat
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
import org.xmlunit.matchers.CompareMatcher.isIdenticalTo
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AlmaControllerTests(
    @Autowired private var webClient: WebTestClient,
    @Autowired private val almaRepository: AlmaRepository,
    @Autowired private val marcXChangeService: MarcXChangeService
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

    @ParameterizedTest
    @ValueSource(
        strings = [
            "12",  // Too short
            "12121212121212121212",  // Too long
            "......." // Invalid characters
        ]
    )
    fun shouldValidateBarcode(barcode: String) {
        webClient.get().uri("/alma/barcode/$barcode")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ProblemDetail::class.java)
            .consumeWith { problemDetail ->
                Assertions.assertEquals(
                    problemDetail.responseBody!!.detail,
                    "getAlmaItemByBarcode.barcode: ${AlmaController.BARCODE_MESSAGE}"
                )
            }
    }

    @Test
    fun `Alma bib response should be mapped to expected Marc record`() {
        val bibResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_result.xml").inputStream,
            Charsets.UTF_8
        )
        val marcRecord = StreamUtils.copyToByteArray(
            ClassPathResource("AlmaXmlTestFiles/marc.xml").inputStream
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(bibResponse)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402")
            .block()!!
        val mappedRecord = marcXChangeService.writeAsByteArray(almaResponse.record, false)
        assertThat(mappedRecord, isIdenticalTo(marcRecord).ignoreWhitespace())
    }

    @Test
    fun `Alma item response should be mapped to expected Marc record`() {
        val itemResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/item_result.xml").inputStream,
            Charsets.UTF_8
        )
        val bibResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/bib_result.xml").inputStream,
            Charsets.UTF_8
        )
        val marcRecord = StreamUtils.copyToByteArray(
            ClassPathResource("AlmaXmlTestFiles/marc_enumchron.xml").inputStream
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(itemResponse)
                .addHeader("Content-type", "application/xml")
        )
        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(bibResponse)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByBarcode(barcode = "b4rc0d3")
            .block()!!
        val mappedRecord = marcXChangeService.writeAsByteArray(almaResponse, false)
        assertThat(mappedRecord, isIdenticalTo(marcRecord).ignoreWhitespace())
    }

    @Test
    fun `An Alma item without MMSID should result in an AlmaException`() {
        val itemResponse = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/item_no_mms.xml").inputStream,
            Charsets.UTF_8
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody(itemResponse)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByBarcode(barcode = "b4rc0d3")

        StepVerifier.create(almaResponse)
            .expectErrorMatches { it is AlmaException && it.message == "No MMS-id found in Alma item result" }
            .verify()
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

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402")

        StepVerifier.create(almaResponse)
            .expectErrorMatches {
                it is AlmaRecordNotFoundException &&
                        it.message == "Input parameters mmsId 987654321098765402 is not valid."
            }
            .verify()
    }

    @Test
    fun `Alma error code 401689 should be mapped to an AlmaRecordNotFoundException`() {
        val bibError = StreamUtils.copyToString(
            ClassPathResource("AlmaXmlTestFiles/item_error_not_found.xml").inputStream,
            Charsets.UTF_8
        )

        mockBackEnd.enqueue(
            MockResponse()
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody(bibError)
                .addHeader("Content-type", "application/xml")
        )

        val almaResponse = almaRepository.getRecordByBarcode(barcode = "1111")

        StepVerifier.create(almaResponse)
            .expectErrorMatches {
                it is AlmaRecordNotFoundException &&
                        it.message == "No items found for barcode 1111."
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

        val almaResponse = almaRepository.getRecordByMMS(mms = "ABCDEF12345")

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

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402")

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

        val almaResponse = almaRepository.getRecordByMMS(mms = "987654321098765402")

        StepVerifier.create(almaResponse)
            .expectErrorMatches { it is AlmaException && it.message == "Server error" }
            .verify()
    }

}
