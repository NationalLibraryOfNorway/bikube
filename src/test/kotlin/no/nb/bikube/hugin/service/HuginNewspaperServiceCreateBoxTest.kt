package no.nb.bikube.hugin.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nb.bikube.api.newspaper.model.ParsedIdResponse
import no.nb.bikube.api.newspaper.service.MaxitService
import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.hugin.model.dbo.Box
import no.nb.bikube.hugin.model.dbo.HuginTitle
import no.nb.bikube.hugin.model.dto.CreateBoxDto
import no.nb.bikube.hugin.repository.BoxRepository
import no.nb.bikube.hugin.repository.NewspaperRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.*

class HuginNewspaperServiceCreateBoxTest {

    private val titleRepository = mockk<TitleRepository>()
    private val boxRepository = mockk<BoxRepository>(relaxed = true)
    private val newspaperService = mockk<NewspaperService>()
    private val newspaperRepository = mockk<NewspaperRepository>()
    private val maxitService = mockk<MaxitService>()

    private val testTitle = HuginTitle().apply { id = 42 }
    private val testDate = LocalDate.of(2026, 4, 15)

    @BeforeEach
    fun commonMocks() {
        every { titleRepository.findById(42) } returns Optional.of(testTitle)
        every { titleRepository.findById(999) } returns Optional.empty()
        every { boxRepository.findAllByTitleIdOrderByDateFromAsc(any()) } returns mutableListOf()
        every { boxRepository.saveAll(any<MutableList<Box>>()) } answers { firstArg() }
        every { boxRepository.flush() } returns Unit
        every { boxRepository.save(any<Box>()) } answers { firstArg() }
    }

    @Nested
    inner class MaxitIdEnabled {

        private val service = lazy {
            HuginNewspaperService(
                titleRepository, boxRepository, newspaperService,
                newspaperRepository, maxitService, true
            )
        }

        @BeforeEach
        fun mockMaxit() {
            every { maxitService.getUniqueIds() } returns Mono.just(ParsedIdResponse("9000001", "NP-009000001"))
        }

        @Test
        fun `createBox uses Maxit priref as box ID when flag is on`() {
            val dto = CreateBoxDto(titleId = 42, dateFrom = testDate)
            val result = service.value.createBox(dto)

            assertEquals("9000001", result.id)
            assertEquals(testDate, result.dateFrom)
            assertEquals(true, result.active)
            verify(exactly = 1) { maxitService.getUniqueIds() }
        }

        @Test
        fun `createBox ignores user-provided ID when flag is on`() {
            val dto = CreateBoxDto(titleId = 42, id = "USER-BOX-1", dateFrom = testDate)
            val result = service.value.createBox(dto)

            assertEquals("9000001", result.id, "Maxit priref should override user-provided ID")
            verify(exactly = 1) { maxitService.getUniqueIds() }
        }

        @Test
        fun `createBox throws when Maxit returns empty`() {
            every { maxitService.getUniqueIds() } returns Mono.empty()

            val dto = CreateBoxDto(titleId = 42, dateFrom = testDate)
            assertThrows<IllegalStateException> { service.value.createBox(dto) }
        }

        @Test
        fun `createBox deactivates existing boxes`() {
            val existingBox = Box(id = "old", dateFrom = testDate.minusDays(30), active = true, title = testTitle)
            every { boxRepository.findAllByTitleIdOrderByDateFromAsc(42) } returns mutableListOf(existingBox)

            val dto = CreateBoxDto(titleId = 42, dateFrom = testDate)
            service.value.createBox(dto)

            assertEquals(false, existingBox.active)
            verify { boxRepository.saveAll(mutableListOf(existingBox)) }
        }
    }

    @Nested
    inner class MaxitIdDisabled {

        private val service = lazy {
            HuginNewspaperService(
                titleRepository, boxRepository, newspaperService,
                newspaperRepository, maxitService, false
            )
        }

        @Test
        fun `createBox uses user-provided ID when flag is off`() {
            val dto = CreateBoxDto(titleId = 42, id = "  MY-BOX-1  ", dateFrom = testDate)
            val result = service.value.createBox(dto)

            assertEquals("MY-BOX-1", result.id, "User ID should be trimmed and used")
            verify(exactly = 0) { maxitService.getUniqueIds() }
        }

        @Test
        fun `createBox throws when ID is missing and flag is off`() {
            val dto = CreateBoxDto(titleId = 42, id = null, dateFrom = testDate)
            assertThrows<IllegalStateException> { service.value.createBox(dto) }
            verify(exactly = 0) { maxitService.getUniqueIds() }
        }
    }

    @Nested
    inner class CommonBehavior {

        private val service = lazy {
            HuginNewspaperService(
                titleRepository, boxRepository, newspaperService,
                newspaperRepository, maxitService, false
            )
        }

        @Test
        fun `createBox throws when title not found`() {
            val dto = CreateBoxDto(titleId = 999, id = "BOX-1", dateFrom = testDate)
            assertThrows<IllegalStateException> { service.value.createBox(dto) }
        }
    }
}

