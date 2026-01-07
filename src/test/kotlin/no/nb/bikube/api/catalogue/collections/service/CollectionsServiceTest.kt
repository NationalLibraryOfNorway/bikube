package no.nb.bikube.api.catalogue.collections.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsLocationModelMock
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.collectionsLocationObjectMock
import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData.Companion.emptyCollectionsLocationModelMock
import no.nb.bikube.api.catalogue.collections.config.CollectionsWebClientConfig
import no.nb.bikube.api.catalogue.collections.model.CollectionsLocationModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.kotlin.test.test
import java.net.URI
import java.util.function.Function

@SpringBootTest
@ActiveProfiles("test")
class CollectionsServiceTest {
    @MockkBean(relaxed = true)
    private lateinit var collectionsWebClient: CollectionsWebClientConfig

    @Autowired
    private lateinit var collectionsService: CollectionsService

    val mockWebClient = mockk<WebClient>(relaxed = true)
    val mockRequestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>(relaxed = true)
    val mockRequestBodyUriSpec = mockk<WebClient.RequestBodyUriSpec>(relaxed = true)
    val mockRequestBodySpec = mockk<WebClient.RequestBodySpec>(relaxed = true)
    val mockResponseSpec = mockk<WebClient.ResponseSpec>(relaxed = true)

    @BeforeEach
    fun beforeEach() {
        every { collectionsWebClient.collectionsWebClient() } returns mockWebClient
        every { mockWebClient.get() } returns mockRequestHeadersUriSpec
        every { mockWebClient.post() } returns mockRequestBodyUriSpec
        every { mockRequestHeadersUriSpec.uri(ofType<Function<UriBuilder, URI>>()) } returns mockRequestHeadersUriSpec
        every { mockRequestBodyUriSpec.uri(ofType<Function<UriBuilder, URI>>()) } returns mockRequestBodySpec
        every { mockRequestBodySpec.contentType(any()) } returns mockRequestBodySpec
        every { mockRequestBodySpec.bodyValue(any()) } returns mockRequestHeadersUriSpec
        every { mockRequestHeadersUriSpec.retrieve() } returns mockResponseSpec
        every { mockResponseSpec.onStatus(any(), any()) } returns mockResponseSpec
        every { mockResponseSpec.bodyToMono<CollectionsLocationModel>() } returns Mono.just(collectionsLocationModelMock)
    }

    @Test
    fun `should return existing container if it exists`() {
        // Given a container with given barcode exists
        val barcode = "barcode"
        val username = "username"
        every { mockResponseSpec.bodyToMono<CollectionsLocationModel>() } returns Mono.just(collectionsLocationModelMock)

        // When trying to create a container
        collectionsService.createContainerIfNotExists(barcode, username)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsLocationObjectMock, it)
            }
            .verifyComplete()

        // Then we should return the existing container and not create new
        verify(exactly = 0) { mockWebClient.post() }
    }

    @Test
    fun `should create new container if barcode does not exist`() {
        // Given a container with given barcode does not exist
        val barcode = "barcode"
        val username = "username"
        every { mockResponseSpec.bodyToMono<CollectionsLocationModel>() } returnsMany listOf(
            Mono.just(emptyCollectionsLocationModelMock),
            Mono.just(collectionsLocationModelMock)
        )

        // When trying to create a container
        collectionsService.createContainerIfNotExists(barcode, username)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsLocationObjectMock, it)
            }
            .verifyComplete()

        // Then should create a new container
        verify(exactly = 1) { mockWebClient.post() }
    }
}
