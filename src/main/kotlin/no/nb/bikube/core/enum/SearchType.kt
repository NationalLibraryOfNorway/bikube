package no.nb.bikube.core.enum

enum class SearchType (val value: String) {
    TITLE("title"),
    ITEM("item"),
    PUBLISHER("publisher"),
    LANGUAGE("language"),
    LOCATION("location")
}

fun getSearchType(searchType: String): SearchType {
    return when (searchType) {
        "title" -> SearchType.TITLE
        "item" -> SearchType.ITEM
        "publisher" -> SearchType.PUBLISHER
        "language" -> SearchType.LANGUAGE
        "location" -> SearchType.LOCATION
        else -> throw Exception("Search type $searchType is not supported.")
    }
}