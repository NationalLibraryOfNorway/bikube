package no.nb.bikube.core.mapper

import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsTermModelMockLocationB
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherPlaceMapperTests {

    val publisherPlaceMock = collectionsTermModelMockLocationB
        .adlibJson
        .recordList!!
        .first()

    @Test
    fun `PublisherPlace mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).databaseId)
    }

    @Test
    fun `PublisherPlace mapper should map name`() {
        Assertions.assertEquals("Oslo", mapCollectionsObjectToGenericPublisherPlace(publisherPlaceMock).name)
    }
}