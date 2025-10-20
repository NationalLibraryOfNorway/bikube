package no.nb.bikube.api.catalogue.alma.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AlmaControllerTests(
    @Autowired private var webClient: WebTestClient
) {

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

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1234",  // Too short
            "1234-5678-910",  // Too long
            "123A-567B" // Invalid characters
        ]
    )
    fun shouldValidateISSN(issn: String) {
        webClient.get().uri("/alma/issn/$issn")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ProblemDetail::class.java)
            .consumeWith { problemDetail ->
                Assertions.assertEquals(
                    problemDetail.responseBody!!.detail,
                    "getMarcRecordsByISSN.issn: ${AlmaController.ISSN_MESSAGE}"
                )
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1234",  // Too short
            "1234-5678-90123456789",  // Too long
            "1234567890ABC" // Invalid characters
        ]
    )
    fun shouldValidateISBN(isbn: String) {
        webClient.get().uri("/alma/isbn/$isbn")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ProblemDetail::class.java)
            .consumeWith { problemDetail ->
                Assertions.assertEquals(
                    problemDetail.responseBody!!.detail,
                    "getMarcRecordsByISBN.isbn: ${AlmaController.ISBN_MESSAGE}"
                )
            }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "M-1234-5678",  // Too short
            "M-1234-5678-90",  // Too long
            "M-1234-56AB" // Invalid characters
        ]
    )
    fun shouldValidateISMN(ismn: String) {
        webClient.get().uri("/alma/ismn/$ismn")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(ProblemDetail::class.java)
            .consumeWith { problemDetail ->
                Assertions.assertEquals(
                    problemDetail.responseBody!!.detail,
                    "getMarcRecordsByISMN.ismn: ${AlmaController.ISMN_MESSAGE}"
                )
            }
    }
}
