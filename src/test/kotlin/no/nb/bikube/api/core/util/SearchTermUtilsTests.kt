package no.nb.bikube.api.core.util

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class SearchTermUtilsTests {

   @Test
    fun `normalizeSearchTerm should replace multiple spaces with single space and trim`() {
        assert(sanitizeSearchTerm("Dette/er+en&test123") == "Dette er en test123")
    }

    @Test
    fun `normalizeSearchTerm should handle norwegian characters`() {
        assert(sanitizeSearchTerm("ÅÆØ/åæø+ test") == "ÅÆØ åæø test")
    }

    @Test
    fun `normalizeSearchTerm should handle multiple whitepace`() {
        assert(sanitizeSearchTerm("   multiple   spaces   ") == "multiple spaces")
    }

}
