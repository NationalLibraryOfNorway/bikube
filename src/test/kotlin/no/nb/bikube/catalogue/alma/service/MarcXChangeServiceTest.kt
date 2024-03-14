package no.nb.bikube.catalogue.alma.service

import no.nb.bikube.catalogue.alma.util.DocumentMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.util.StreamUtils

@SpringBootTest
class MarcXChangeServiceTest(
    @Autowired val marcXChangeService: MarcXChangeService,
    @Autowired val documentMapper: DocumentMapper
) {

    private val bibResponse = StreamUtils.copyToString(
        ClassPathResource("AlmaXmlTestFiles/bib_result.xml").inputStream,
        Charsets.UTF_8
    )

    private val marcRecord = StreamUtils.copyToByteArray(
        ClassPathResource("AlmaXmlTestFiles/bib_marc.xml").inputStream
    )

    @Test
    fun `Alma bib response should be mapped to MarcRecord`() {
        val bibResponse = marcXChangeService.parseBibResult(bibResponse)
        val recordBytes = marcXChangeService.writeAsByteArray(bibResponse.record, false)

        Assertions.assertEquals(
            documentMapper.parseDocument(recordBytes),
            documentMapper.parseDocument(marcRecord)
        )
    }

}
