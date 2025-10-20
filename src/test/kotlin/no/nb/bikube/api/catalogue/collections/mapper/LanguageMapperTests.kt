package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsTermModelMockLanguageA
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LanguageMapperTests {

    val languageMockA = collectionsTermModelMockLanguageA.getFirstObject()

    @Test
    fun `Language mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericLanguage(languageMockA).catalogueId)
    }

    @Test
    fun `Language mapper should map name`() {
        Assertions.assertEquals("nob", mapCollectionsObjectToGenericLanguage(languageMockA).name)
    }
}
