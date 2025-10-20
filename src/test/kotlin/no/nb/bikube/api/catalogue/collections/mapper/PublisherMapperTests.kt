package no.nb.bikube.api.catalogue.collections.mapper

import no.nb.bikube.api.catalogue.collections.CollectionsModelMockData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherMapperTests {

    val publisherPlaceMock = CollectionsModelMockData.Companion.collectionsNameModelMockA.getFirstObject()

    @Test
    fun `Publisher mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericPublisher(publisherPlaceMock).catalogueId)
    }

    @Test
    fun `Publisher mapper should map name`() {
        Assertions.assertEquals("Schibsted", mapCollectionsObjectToGenericPublisher(publisherPlaceMock).name)
    }
}
