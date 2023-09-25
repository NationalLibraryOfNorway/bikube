package no.nb.bikube.core.util

import java.time.LocalDate

val yearRegex = Regex("^\\d{4}$")
val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")

class DateUtils {
    companion object {
        fun parseYearOrDate(date: String?): LocalDate? {
            return if (date!!.matches(yearRegex)) {
                LocalDate.parse("$date-01-01")
            } else if (date!!.matches(dateRegex)) {
                LocalDate.parse(date)
            } else {
                logger().warn("Could not parse date: $date")
                null
            }
        }
    }
}
