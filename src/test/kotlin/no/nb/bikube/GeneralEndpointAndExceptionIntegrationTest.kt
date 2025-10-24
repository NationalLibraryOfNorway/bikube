package no.nb.bikube

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.catalogue.collections.exception.CollectionsException
import no.nb.bikube.api.catalogue.collections.exception.CollectionsItemNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsManifestationNotFound
import no.nb.bikube.api.catalogue.collections.exception.CollectionsTitleNotFound
import no.nb.bikube.api.catalogue.collections.repository.CollectionsRepository
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
import org.springframework.http.ProblemDetail
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GeneralEndpointAndExceptionIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    private fun getItemProblem(): Pair<Int, ProblemDetail> {
        val res = mockMvc
            .get("/bikube/item") {
                param("catalogueId", "1")
                param("materialType", MaterialType.NEWSPAPER.name)
            }
            .andReturn()

        val status = res.response.status
        val problem = objectMapper.readValue(res.response.contentAsString, ProblemDetail::class.java)
        return status to problem
    }

    private fun parseTimestamp(timestamp: String): LocalDate =
        LocalDate.parse(timestamp.take(10))

    private val expectedInstance = URI("/bikube/item")
    private val type400 = URI("https://produksjon.nb.no/bikube/error/bad-request")
    private val type404 = URI("https://produksjon.nb.no/bikube/error/not-found")
    private val type409 = URI("https://produksjon.nb.no/bikube/error/conflict")
    private val type500 = URI("https://produksjon.nb.no/bikube/error/internal-server-error")

    @Test
    fun `CollectionsException should return 500 with proper ProblemDetail`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsException(null))

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsTitleNotFound(null))

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(NotSupportedException(null))

        val (status, problem) = getItemProblem()
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), status)
        Assertions.assertEquals(type400, problem.type)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), problem.status)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, problem.title)
        Assertions.assertTrue(problem.detail!!.lowercase().contains("not supported"))
        Assertions.assertEquals(expectedInstance, problem.instance)
        Assertions.assertEquals(LocalDate.now(), parseTimestamp(problem.properties!!["timestamp"] as String))
    }

    @Test
    fun `BadRequestBodyException should return 400 with proper ProblemDetail`() {
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(BadRequestBodyException(null))

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(RecordAlreadyExistsException(null))

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsItemNotFound(null))

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
        every { collectionsRepository.getSingleCollectionsModelWithoutChildren(any()) } returns Mono.error(CollectionsManifestationNotFound(null))

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
            .get("/")
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl("/bikube/swagger-ui.html") }

        mockMvc
            .get("/swagger-ui.html")
            .andExpect { status { is3xxRedirection() } }
            .andExpect { redirectedUrl("/bikube/webjars/swagger-ui/index.html") }
    }
}
