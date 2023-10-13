package no.nb.bikube.core.mapper

import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import no.nb.bikube.core.model.collections.getFirstObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherPlaceMapperTests {

    val publisherPlaceMock = collectionsTermModelMockLocationB.getFirstObject()!!

    @Test
    fun `PublisherPlace mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).databaseId)
    }

    @Test
    fun `PublisherPlace mapper should map name`() {
        Assertions.assertEquals("Oslo", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).name)
    }
}
