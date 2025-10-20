package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherPlaceMapperTests {

    val publisherPlaceMock = collectionsTermModelMockLocationB.getFirstObject()

    @Test
    fun `PublisherPlace mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).catalogueId)
    }

    @Test
    fun `PublisherPlace mapper should map name`() {
        Assertions.assertEquals("Oslo", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).name)
    }
}
