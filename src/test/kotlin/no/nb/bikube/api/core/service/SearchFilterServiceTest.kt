package no.nb.bikube.api.core.service

import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.core.model.Title
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.collections.get

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
        endDate = LocalDate.parse("1999-12-31"),
        catalogueId = "3",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val titleD = Title(
        name = "News from brakka",
        startDate = LocalDate.parse("2000-01-01"),
        endDate = LocalDate.parse("2019-01-01"),
        catalogueId = "4",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val titleE = Title(
        name = "VG",
        startDate = LocalDate.parse("2005-01-01"),
        endDate = null,
        catalogueId = "5",
        publisher = null,
        publisherPlace = null,
        language = null,
        materialType = MaterialType.NEWSPAPER.norwegian
    )

    private val results: List<Title> = listOf(titleA, titleB, titleC, titleD, titleE)

    @Test
    fun `filterSearchResults should filter by date`() {
        val date1 = LocalDate.parse("1990-01-01")
        val filteredResults1 = searchFilterService.filterSearchResults(results, "", date1, false)
        Assertions.assertEquals(filteredResults1.size, 1)
        Assertions.assertTrue(filteredResults1.contains(titleB))

        val date2 = LocalDate.parse("1950-01-01")
        val filteredResults2 = searchFilterService.filterSearchResults(results, "", date2, true)
        Assertions.assertEquals(filteredResults2.size, 1)
        Assertions.assertTrue(filteredResults2.contains(titleB))

        val date3 = LocalDate.parse("1999-01-01")
        val filteredResults3 = searchFilterService.filterSearchResults(results, "", date3, false)
        Assertions.assertEquals(filteredResults3.size,2)
        Assertions.assertTrue(filteredResults3.contains(titleB))
        Assertions.assertTrue(filteredResults3.contains(titleC))

        val date4 = LocalDate.parse("2006-01-01")
        val filteredResults4 = searchFilterService.filterSearchResults(results, "", date4, false)
        println("filteredResults4: $filteredResults4")
        Assertions.assertEquals(filteredResults4.size, 2)
        Assertions.assertTrue(filteredResults4.contains(titleD))
        Assertions.assertTrue(filteredResults4.contains(titleE))

        val date5 = LocalDate.parse("2022-01-01")
        val filteredResults5 = searchFilterService.filterSearchResults(results, "", date5, false)
        Assertions.assertEquals(filteredResults5.size, 1)
        Assertions.assertTrue(filteredResults5.contains(titleE))
    }

    @Test
    fun `filterSearchResults should select best match`() {
        val searchTerm = "B-Avisen"
        val filteredResults = searchFilterService.filterSearchResults(results, searchTerm, null, true)
        Assertions.assertEquals(filteredResults.size, 1)
        Assertions.assertTrue(filteredResults.contains(titleB))
    }

    @Test
    fun `filterSearchResults should return original results list if best match has too low score`() {
        val searchTerm = "En helt annen tittel" // Gives 45 score
        val filteredResults = searchFilterService.filterSearchResults(results, searchTerm, null, true)
        Assertions.assertEquals(filteredResults.size, 5)
        Assertions.assertTrue(filteredResults.contains(titleB))
    }

    @Test
    fun `filterSearchResults should return list sorted by highest score in descending order`() {
        val searchTerm = "Avis"
        val filteredResults = searchFilterService.filterSearchResults(results, searchTerm, null, false)
        Assertions.assertEquals(filteredResults.size, 5)
        Assertions.assertEquals(filteredResults[0], titleA)
        Assertions.assertEquals(filteredResults[1], titleB)
        Assertions.assertEquals(filteredResults[2], titleC)
        Assertions.assertEquals(filteredResults[3], titleE)
        Assertions.assertEquals(filteredResults[4], titleD)
    }

}
