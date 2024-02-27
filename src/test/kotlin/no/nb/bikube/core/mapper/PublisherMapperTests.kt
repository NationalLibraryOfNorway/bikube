package no.nb.bikube.core.mapper

import no.nb.bikube.catalog.collections.mapper.mapCollectionsObjectToGenericPublisher
import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsNameModelMockA
import no.nb.bikube.catalog.collections.model.getFirstObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherMapperTests {

    val publisherPlaceMock = collectionsNameModelMockA.getFirstObject()!!

    @Test
    fun `Publisher mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericPublisher(publisherPlaceMock).databaseId)
    }

    @Test
    fun `Publisher mapper should map name`() {
        Assertions.assertEquals("Schibsted", mapCollectionsObjectToGenericPublisher(publisherPlaceMock).name)
    }
}
