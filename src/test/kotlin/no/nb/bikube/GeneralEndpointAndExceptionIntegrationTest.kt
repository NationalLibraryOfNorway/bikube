package no.nb.bikube

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.catalogue.collections.exception.CollectionsException
import no.nb.bikube.api.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.exception.NotSupportedException
import no.nb.bikube.api.core.exception.RecordAlreadyExistsException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeneralEndpointAndExceptionIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockkBean
    private lateinit var collectionsService: CollectionsService

    private fun getItemProblem(): Pair<Int, ProblemDetail> {
        val res = mockMvc.perform(
            MockMvcRequestBuilders.get("/bikube/api/item")
                .contextPath("/bikube")
                .param("catalogueId", "1")
                .param("materialType", MaterialType.NEWSPAPER.name)
                .accept(
                    MediaType.APPLICATION_PROBLEM_JSON,
                    MediaType.APPLICATION_JSON
                )
        ).andReturn()

        val dispatched = mockMvc
            .perform(asyncDispatch(res))
            .andReturn()

        val status = dispatched.response.status
        val body = dispatched.response.contentAsString
        require(body.isNotBlank()) { "Expected ProblemDetail JSON but got empty body. status=$status" }

        val problem = objectMapper.readValue(body, ProblemDetail::class.java)
        return status to problem
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

    private fun parseTimestamp(timestamp: String): LocalDate =
        LocalDate.parse(timestamp.take(10))

    private val expectedInstance = URI("/bikube/api/item")
    private val type400 = URI("https://produksjon.nb.no/bikube/error/bad-request")
    private val type404 = URI("https://produksjon.nb.no/bikube/error/not-found")
    private val type409 = URI("https://produksjon.nb.no/bikube/error/conflict")
    private val type500 = URI("https://produksjon.nb.no/bikube/error/internal-server-error")

    @Test
    fun `CollectionsException should return 500 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsException(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status)
        Assertions.assertEquals(type500, problem.type)
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.status)
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("error"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("collections"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsTitleNotFound should return 404 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsTitleNotFound(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), status)
        Assertions.assertEquals(type404, problem.type)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("not found"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("collections"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `NotSupportedException should return 400 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(NotSupportedException(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), status)
        Assertions.assertEquals(type400, problem.type)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("not supported"))
        Assertions.assertEquals(expectedInstance, problem.instance)
    }

    @Test
    fun `BadRequestBodyException should return 400 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(
            BadRequestBodyException(null)
        )

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), status)
        Assertions.assertEquals(type400, problem.type)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("body is malformed"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `RecordAlreadyExistsException should return 409 conflict with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(RecordAlreadyExistsException(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), status)
        Assertions.assertEquals(type409, problem.type)
        Assertions.assertEquals(HttpStatus.CONFLICT.value(), problem.status)
        Assertions.assertEquals(HttpStatus.CONFLICT.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("already exists"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsItemNotFound should return 404 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsItemNotFound(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), status)
        Assertions.assertEquals(type404, problem.type)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("not found"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("item"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("collections"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsManifestationNotFound should return 404 with proper ProblemDetail`() {
        every { collectionsService.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsManifestationNotFound(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), status)
        Assertions.assertEquals(type404, problem.type)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        Assertions.assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("not found"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("manifestation"))
        Assertions.assertTrue(problem.detail!!.lowercase().contains("collections"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `should redirect from base path to swagger without login`() {
        mockMvc
            .perform(get("/"))
            .andExpect(status().isMovedPermanently())
            .andExpect(redirectedUrl("/bikube/swagger-ui/index.html"))
    }
}
