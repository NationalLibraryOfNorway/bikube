package no.nb.bikube.api.core.util

// Utility function to sanitize search terms by removing special characters and replacing them with spaces
fun sanitizeSearchTerm(input: String): String {
    return input
        .replace(Regex("[^\\p{L}\\p{N}]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
