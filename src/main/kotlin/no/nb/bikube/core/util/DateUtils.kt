package no.nb.bikube.core.util

import java.time.LocalDate

val yearRegex = Regex("^\\d{4}$")
val dateWithSlashRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
val dateWithDotsRegex = Regex("^\\d{4}\\.\\d{2}\\.\\d{2}$")

class DateUtils {
    companion object {
        fun parseYearOrDate(date: String?): LocalDate? {
            return if (date.isNullOrBlank()) {
                null
            } else if (date.matches(yearRegex)) {
                LocalDate.parse("$date-01-01")
            } else if (date.matches(dateWithSlashRegex)) {
                LocalDate.parse(date)
            } else if (date.matches(dateWithDotsRegex)){
                LocalDate.parse(date.replace(".", "-"))
            } else {
                logger().warn("Could not parse date: $date")
                null
            }
        }
    }
}
