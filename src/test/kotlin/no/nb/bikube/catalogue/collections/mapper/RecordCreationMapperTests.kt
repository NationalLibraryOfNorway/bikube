package no.nb.bikube.catalogue.collections.mapper

import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.collectionsModelMockTitleA
import no.nb.bikube.catalogue.collections.model.getFirstObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class RecordCreationMapperTests {

    @Test
    fun `RecordCreation mapper should map collectionsObject to collectionsPartObject`() {
        val original = collectionsModelMockTitleA.getFirstObject()
        val mappedObj = mapCollectionsObjectToCollectionsPartObject(original!!).partsReference!!

        Assertions.assertEquals(original.priRef, mappedObj.priRef)
        Assertions.assertEquals(original.datingList, mappedObj.dateStart)
        Assertions.assertEquals(original.recordTypeList, mappedObj.recordType)
        Assertions.assertEquals(original.partsList, mappedObj.partsList)
        Assertions.assertEquals(original.titleList, mappedObj.titleList)
        Assertions.assertEquals(original.formatList, mappedObj.formatList)
    }
}
