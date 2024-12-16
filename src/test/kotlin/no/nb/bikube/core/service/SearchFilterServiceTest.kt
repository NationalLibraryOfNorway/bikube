package no.nb.bikube.core.service

import no.nb.bikube.core.enum.MaterialType
import no.nb.bikube.core.model.Title
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class SearchFilterServiceTest {
    @Autowired
    private lateinit var searchFilterService: SearchFilterService

    private val titleA: Title = Title(
        name = "A-Avisen",
        startDate = LocalDate.parse("1900-01-01"),
        endDate = LocalDate.parse("1925-12-31"),
        catalogueId = "1",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val titleB = Title(
        name = "B-Avisen",
        startDate = LocalDate.parse("1950-01-01"),
        endDate = LocalDate.parse("1999-12-31"),
        catalogueId = "2",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val titleC = Title(
        name = "C-Avisen",
        startDate = LocalDate.parse("1998-01-01"),
        endDate = null,
        catalogueId = "3",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val results: List<Title> = listOf(titleA, titleB, titleC)

    @Test
    fun `filterSearchResults should filter by date`() {
        val date1 = LocalDate.parse("1990-01-01")
        val filteredResults1 = searchFilterService.filterSearchResults(results, "", date1, false)
        assert(filteredResults1.size == 1)
        assert(filteredResults1.contains(titleB))

        val date2 = LocalDate.parse("1950-01-01")
        val filteredResults2 = searchFilterService.filterSearchResults(results, "", date2, true)
        assert(filteredResults2.size == 1)
        assert(filteredResults2.contains(titleB))

        val date3 = LocalDate.parse("1999-01-01")
        val filteredResults3 = searchFilterService.filterSearchResults(results, "", date3, false)
        assert(filteredResults3.size == 2)
        assert(filteredResults3.contains(titleB))
        assert(filteredResults3.contains(titleC))
    }

    @Test
    fun `filterSearchResults should select best match`() {
        val searchTerm = "B-Avisen"
        val filteredResults = searchFilterService.filterSearchResults(results, searchTerm, null, true)
        assert(filteredResults.size == 1)
        assert(filteredResults.contains(titleB))
    }

    @Test
    fun `filterSearchResults should return original results list if best match has too low score`() {
        val searchTerm = "En helt annen tittel" // Gives 45 score
        val filteredResults = searchFilterService.filterSearchResults(results, searchTerm, null, true)
        assert(filteredResults.size == 3)
        assert(filteredResults.containsAll(listOf(titleA, titleB, titleC)))
    }

}