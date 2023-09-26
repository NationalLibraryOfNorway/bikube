package no.nb.bikube.core.util

import no.nb.bikube.core.util.DateUtils.Companion.parseYearOrDate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class DateUtilsTests {
    @Test
    fun`parseYearOrDate should parse years like 2020 to first date of year`() {
        Assertions.assertEquals(LocalDate.parse("2020-01-01"), parseYearOrDate("2020"))
    }

    @Test
    fun`parseYearOrDate should parse dates like 2020-01-08`() {
        Assertions.assertEquals(LocalDate.parse("2020-01-08"), parseYearOrDate("2020-01-08"))
    }

    @Test
    fun`parseYearOrDate should parse dates like 2020dot01dot09`() {
        Assertions.assertEquals(LocalDate.parse("2020-01-09"), parseYearOrDate("2020.01.09"))
    }

    @Test
    fun`parseYearOrDate should return null for other kinds of dates`() {
        Assertions.assertEquals(null, parseYearOrDate("2020/01/01"))
        Assertions.assertEquals(null, parseYearOrDate("01-01-2020"))
        Assertions.assertEquals(null, parseYearOrDate("01.01.2020"))
        Assertions.assertEquals(null, parseYearOrDate("01012020"))
        Assertions.assertEquals(null, parseYearOrDate(null))
        Assertions.assertEquals(null, parseYearOrDate(""))
    }

}
