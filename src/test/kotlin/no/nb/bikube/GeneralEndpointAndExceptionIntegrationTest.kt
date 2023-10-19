package no.nb.bikube

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.exception.*
import no.nb.bikube.newspaper.repository.AxiellRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.net.URI
import java.time.Duration
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GeneralEndpointAndExceptionIntegrationTest (
    @Autowired private var webClient: WebTestClient
){
    @MockkBean
    private lateinit var axiellRepository: AxiellRepository

    private fun getItem(): ResponseSpec {
        return webClient
            .get()
            .uri { uri ->
                uri.pathSegment("item")
                    .queryParam("catalogueId", 1)
                    .queryParam("materialType", MaterialType.NEWSPAPER)
                    .build()
            }
            .exchange()
    }

    private fun parseTimestamp(timestamp: String): LocalDate {
        return LocalDate.parse(timestamp.take(10))
    }

    /*
        Modern APIs return ProblemDetail as their problem response, and it is generally considered as best practice.
        RFC 9457 describes ProblemDetail, and what it should always contain:

        type: URI reference (default "about:blank", preferably a URI describing the problem. Could be a non-resolvable URI)
        status: HTTP status code
        title: Short, human-readable summary of the problem type
        detail: Human-readable explanation specific to this occurrence of the problem
        instance: URI reference that identifies the specific occurrence of the problem (the endpoint that was called)

        Other properties are allowed, but not required. We have decided to always add timestamp as it is useful for debugging and more.
        These integration checks should check that all these 6 properties are present and according to our standards.

        RFC reference: https://www.rfc-editor.org/rfc/rfc9457
    */

    private val expectedInstance = URI("/bikube/item")
    private val type400 = URI("https://produksjon.nb.no/bikube/error/bad-request")
    private val type404 = URI("https://produksjon.nb.no/bikube/error/not-found")
    private val type409 = URI("https://produksjon.nb.no/bikube/error/conflict")
    private val type500 = URI("https://produksjon.nb.no/bikube/error/internal-server-error")

    @BeforeEach
    fun beforeEach( ){
        // Needed to run properly in GitHub Actions
        webClient = webClient.mutate().responseTimeout(Duration.ofSeconds(60)).build()
    }

    @Test
    fun `AxiellCollectionsException should return 500 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(AxiellCollectionsException(null))
        getItem()
            .expectStatus().is5xxServerError
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type500, it.type)
                Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), it.status)
                Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("error"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("collections"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `AxiellTitleNotFound should return 404 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(AxiellTitleNotFound(null))
        getItem()
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type404, it.type)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("not found"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("collections"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `NotSupportedException should return 400 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(NotSupportedException(null))
        getItem()
            .expectStatus().isBadRequest
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type400, it.type)
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("not supported"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `BadRequestBodyException should return 400 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(BadRequestBodyException(null))
        getItem()
            .expectStatus().isBadRequest
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type400, it.type)
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), it.status)
                Assertions.assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("body is malformed"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `RecordAlreadyExistsException should return 409 conflict with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(RecordAlreadyExistsException(null))
        getItem()
            .expectStatus().is4xxClientError
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type409, it.type)
                Assertions.assertEquals(HttpStatus.CONFLICT.value(), it.status)
                Assertions.assertEquals(HttpStatus.CONFLICT.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("already exists"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `AxiellItemNotFound should return 404 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(AxiellItemNotFound(null))
        getItem()
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type404, it.type)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("not found"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("item"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("collections"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `AxiellManifestationNotFound should return 404 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(AxiellManifestationNotFound(null))
        getItem()
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type404, it.type)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("not found"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("manifestation"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("collections"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }

    @Test
    fun `AxiellYearWorkNotFound should return 404 with proper ProblemDetail`() {
        every { axiellRepository.getSingleCollectionsModel(any()) } returns Mono.error(AxiellYearWorkNotFound(null))
        getItem()
            .expectStatus().isNotFound
            .returnResult<ProblemDetail>()
            .responseBody
            .test()
            .assertNext {
                Assertions.assertEquals(type404, it.type)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), it.status)
                Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, it.title)
                Assertions.assertTrue(it.detail!!.lowercase().contains("not found"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("year work"))
                Assertions.assertTrue(it.detail!!.lowercase().contains("collections"))
                Assertions.assertEquals(expectedInstance, it.instance)
                Assertions.assertEquals(LocalDate.now(), parseTimestamp(it.properties!!["timestamp"] as String))
            }
            .verifyComplete()
    }
}
