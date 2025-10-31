package no.nb.bikube

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.catalogue.collections.exception.CollectionsException
import no.nb.bikube.api.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.exception.BadRequestBodyException
import no.nb.bikube.api.core.exception.NotSupportedException
import no.nb.bikube.api.core.exception.RecordAlreadyExistsException
import no.nb.bikube.api.newspaper.service.NewspaperService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeneralEndpointAndExceptionIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockkBean
    lateinit var newspaperService: NewspaperService

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

    private fun parseTimestamp(timestamp: String): LocalDate =
        LocalDate.parse(timestamp.take(10))

    private val expectedInstance = URI("/bikube/api/item")
    private val type400 = URI("https://produksjon.nb.no/bikube/error/bad-request")
    private val type404 = URI("https://produksjon.nb.no/bikube/error/not-found")
    private val type409 = URI("https://produksjon.nb.no/bikube/error/conflict")
    private val type500 = URI("https://produksjon.nb.no/bikube/error/internal-server-error")

    @Test
    fun `CollectionsException should return 500 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            CollectionsException(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), status)
        assertEquals(type500, problem.type)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.status)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("error"))
        assertTrue(problem.detail!!.lowercase().contains("collections"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsTitleNotFound should return 404 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            CollectionsTitleNotFound(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.NOT_FOUND.value(), status)
        assertEquals(type404, problem.type)
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("not found"))
        assertTrue(problem.detail!!.lowercase().contains("collections"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `NotSupportedException should return 400 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            NotSupportedException(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.BAD_REQUEST.value(), status)
        assertEquals(type400, problem.type)
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
        assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("not supported"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `BadRequestBodyException should return 400 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            BadRequestBodyException(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.BAD_REQUEST.value(), status)
        assertEquals(type400, problem.type)
        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
        assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("body is malformed"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `RecordAlreadyExistsException should return 409 conflict with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            RecordAlreadyExistsException(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.CONFLICT.value(), status)
        assertEquals(type409, problem.type)
        assertEquals(HttpStatus.CONFLICT.value(), problem.status)
        assertEquals(HttpStatus.CONFLICT.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("already exists"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsItemNotFound should return 404 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            CollectionsItemNotFound(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.NOT_FOUND.value(), status)
        assertEquals(type404, problem.type)
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("not found"))
        assertTrue(problem.detail!!.lowercase().contains("item"))
        assertTrue(problem.detail!!.lowercase().contains("collections"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `CollectionsManifestationNotFound should return 404 with proper ProblemDetail`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.error(
            CollectionsManifestationNotFound(null)
        )

        val (status, problem) = getItemProblem()
        assertEquals(HttpStatus.NOT_FOUND.value(), status)
        assertEquals(type404, problem.type)
        assertEquals(HttpStatus.NOT_FOUND.value(), problem.status)
        assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, problem.title)
        assertTrue(problem.detail!!.lowercase().contains("not found"))
        assertTrue(problem.detail!!.lowercase().contains("manifestation"))
        assertTrue(problem.detail!!.lowercase().contains("collections"))
        assertEquals(expectedInstance, problem.instance)
        assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `should redirect from base path to swagger without login`() {
        mockMvc
            .perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/swagger-ui/index.html"))
    }

}
