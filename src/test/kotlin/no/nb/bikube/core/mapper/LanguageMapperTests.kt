package no.nb.bikube.core.mapper

import no.nb.bikube.core.CollectionsModelMockData.Companion.collectionsModelMockItemA
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class LanguageMapperTests {

    val languageMockA = collectionsModelMockItemA.adlibJson.recordList!!.first().copy(term = "nob", priRef = "123")

    @Test
    fun `Language mapper should map catalogueId`() {
        Assertions.assertEquals("123", mapCollectionsObjectToGenericLanguage(languageMockA).catalogueId)
    }

    @Test
    fun `Language mapper should map name`() {
        Assertions.assertEquals("nob", mapCollectionsObjectToGenericLanguage(languageMockA).name)
    }
}