package no.nb.bikube.core.mapper

import no.nb.bikube.core.CollectionsModelMockData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class PublisherMapperTests {

    val publisherPlaceMock = CollectionsModelMockData
        .collectionsModelMockItemA
        .adlibJson
        .recordList!!
        .first()
        .copy(term = "Schibsted", priRef = "123")

    @Test
    fun `Publisher mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericLanguage(publisherPlaceMock).catalogueId)
    }

    @Test
    fun `Publisher mapper should map name`() {
        Assertions.assertEquals("nob", mapCollectionsObjectToGenericLanguage(publisherPlaceMock).name)
    }
}