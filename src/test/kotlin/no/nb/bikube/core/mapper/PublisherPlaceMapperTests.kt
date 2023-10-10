package no.nb.bikube.core.mapper

import no.nb.bikube.core.CollectionsModelMockData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherPlaceMapperTests {

    val publisherPlaceMock = CollectionsModelMockData
        .collectionsModelMockItemA
        .adlibJson
        .recordList!!
        .first()
        .copy(term = "Oslo", priRef = "123")

    @Test
    fun `PublisherPlace mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericLanguage(publisherPlaceMock).catalogueId)
    }

    @Test
    fun `PublisherPlace mapper should map name`() {
        Assertions.assertEquals("Oslo", mapCollectionsObjectToGenericLanguage(publisherPlaceMock).name)
    }
}