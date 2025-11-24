package no.nb.bikube.core.service

import me.xdrop.fuzzywuzzy.FuzzySearch
import no.nb.bikube.core.model.Title
import no.nb.bikube.core.util.logger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchFilterService {
    fun filterSearchResults(searchResult: List<Title>, searchTerm: String, date: LocalDate?, selectBestMatch: Boolean?): List<Title> {
        val filteredResult = date?.let {
            searchResult.filter { title ->
                (title.startDate == null || !it.isBefore(title.startDate)) &&
                        (title.endDate == null || !it.isAfter(title.endDate))
            }
        } ?: searchResult

        val scores = FuzzySearch.extractAll(searchTerm, filteredResult.map { it.name })
        logger().debug("Fuzzy search scores {}", scores)

        val bestMatch = scores.maxByOrNull { it.score }
        logger().debug("Fuzzy search best match {}", bestMatch)

        return if (selectBestMatch == true && bestMatch != null && bestMatch.score > 50) {
            listOf(filteredResult[bestMatch.index])
        } else {
            filteredResult.sortedByDescending { title -> scores.find { it.string == title.name }?.score ?: 0 }
        }
    }
}