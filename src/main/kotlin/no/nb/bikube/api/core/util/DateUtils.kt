package no.nb.bikube.api.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

val yearRegex = Regex("^\\d{4}$")
val dateWithDashShortRegex = Regex("^\\d{4}-\\d{2}$")
val dateWithDashRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
val dateWithDotsRegex = Regex("^\\d{4}\\.\\d{2}\\.\\d{2}$")

class DateUtils {
    companion object {
        @JvmStatic
        fun parseYearOrDate(date: String?): LocalDate? {
            return if (date.isNullOrBlank()) {
                null
            } else if (date.matches(yearRegex)) {
                LocalDate.parse("$date-01-01")
            }  else if (date.matches(dateWithDashShortRegex)) {
                LocalDate.parse("$date-01")
            } else if (date.matches(dateWithDashRegex)) {
                LocalDate.parse(date)
            } else if (date.matches(dateWithDotsRegex)){
                LocalDate.parse(date.replace(".", "-"))
            } else {
                logger().warn("Could not parse date: $date")
                null
            }
        }

        fun createDateString(date: LocalDate): String? {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)
        }
    }
}
