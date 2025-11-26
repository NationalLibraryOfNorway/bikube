package no.nb.bikube.api.catalogue.alma.service

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.StreamUtils
import org.xmlunit.matchers.CompareMatcher.isIdenticalTo

@SpringBootTest
@ActiveProfiles("test")
class MarcXChangeServiceTest(
    @Autowired val marcXChangeService: MarcXChangeService
) {

    private val bibResponse = StreamUtils.copyToString(
        ClassPathResource("AlmaXmlTestFiles/bib_result.xml").inputStream,
        Charsets.UTF_8
    )

    private val marcRecord = StreamUtils.copyToByteArray(
        ClassPathResource("AlmaXmlTestFiles/marc.xml").inputStream
    )

    @Test
    fun `Alma bib response should be mapped to MarcRecord`() {
        val bibResponse = marcXChangeService.parseBibResult(bibResponse)
        val recordBytes = marcXChangeService.writeAsByteArray(bibResponse.record, false)

        assertThat(marcRecord, isIdenticalTo(recordBytes).ignoreWhitespace())
    }

}
