package no.nb.bikube.catalogue.collections.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsLocationModelMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsLocationObjectMock
import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.emptyCollectionsLocationModelMock
import no.nb.bikube.catalogue.collections.repository.CollectionsRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

@SpringBootTest
@ActiveProfiles("test")
class CollectionsLocationServiceTest {

    @Autowired
    private lateinit var collectionsLocationService: CollectionsLocationService

    @MockkBean
    private lateinit var collectionsRepository: CollectionsRepository

    @Test
    fun `should return existing container if it exists`() {
        // Given a container with given barcode exists
        val barcode = "barcode"
        val username = "username"
        every { collectionsRepository.searchLocationAndContainers(barcode) } returns Mono.just(collectionsLocationModelMock)

        // When trying to create a container
        collectionsLocationService.createContainerIfNotExists(barcode, username)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsLocationObjectMock, it)
            }
            .verifyComplete()

        // Then we should return the existing container and not create new
        verify (exactly = 1) { collectionsRepository.searchLocationAndContainers(barcode) }
        verify (exactly = 0) { collectionsRepository.createLocationRecord(any()) }
    }

    @Test
    fun `should create new container if barcode does not exist`() {
        // Given a container with given barcode does not exist
        val barcode = "barcode"
        val username = "username"
        every { collectionsRepository.searchLocationAndContainers(barcode) } returns Mono.just(emptyCollectionsLocationModelMock)
        every { collectionsRepository.createLocationRecord(any()) } returns Mono.just(collectionsLocationModelMock)

        // When trying to create a container
        collectionsLocationService.createContainerIfNotExists(barcode, username)
            .test()
            .expectSubscription()
            .assertNext {
                Assertions.assertEquals(collectionsLocationObjectMock, it)
            }
            .verifyComplete()

        // Then should create a new container
        verify (exactly = 1) { collectionsRepository.searchLocationAndContainers(barcode) }
        verify (exactly = 1) { collectionsRepository.createLocationRecord(any()) }
    }
}
