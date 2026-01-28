package no.nb.bikube.api.catalogue.alma.controller

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AlmaControllerTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val jsonMapper: JsonMapper,
) {

    private fun expectProblemDetail(path: String, expectedDetail: String) {
        val result = mockMvc
            .get(path)
            .andExpect {
                status { isBadRequest() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            }
            .andReturn()

        val problem = jsonMapper.readValue(result.response.contentAsString, ProblemDetail::class.java)
        Assertions.assertEquals(expectedDetail, problem.detail)
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
        expectProblemDetail(
            "/api/alma/mms/$mms",
            "getAlmaItemByMMS.mms: ${AlmaController.MMS_MESSAGE}"
        )
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
        expectProblemDetail(
            "/api/alma/barcode/$barcode",
            "getAlmaItemByBarcode.barcode: ${AlmaController.BARCODE_MESSAGE}"
        )
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
        expectProblemDetail(
            "/api/alma/issn/$issn",
            "getMarcRecordsByISSN.issn: ${AlmaController.ISSN_MESSAGE}"
        )
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
        expectProblemDetail(
            "/api/alma/isbn/$isbn",
            "getMarcRecordsByISBN.isbn: ${AlmaController.ISBN_MESSAGE}"
        )
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
        expectProblemDetail(
            "/api/alma/ismn/$ismn",
            "getMarcRecordsByISMN.ismn: ${AlmaController.ISMN_MESSAGE}"
        )
    }
}
