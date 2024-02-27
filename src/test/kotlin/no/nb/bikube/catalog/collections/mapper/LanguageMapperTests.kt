package no.nb.bikube.catalog.collections.mapper

import no.nb.bikube.catalog.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import no.nb.bikube.catalog.collections.model.getFirstObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LanguageMapperTests {

    val languageMockA = collectionsTermModelMockLanguageA.getFirstObject()!!

    @Test
    fun `Language mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericLanguage(languageMockA).databaseId)
    }

    @Test
    fun `Language mapper should map name`() {
        Assertions.assertEquals("nob", mapCollectionsObjectToGenericLanguage(languageMockA).name)
    }
}
