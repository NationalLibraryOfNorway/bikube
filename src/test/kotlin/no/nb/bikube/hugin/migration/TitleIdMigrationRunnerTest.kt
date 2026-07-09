package no.nb.bikube.hugin.migration

import io.mockk.*
import no.nb.bikube.api.catalogue.collections.config.CollectionsLrefConfig
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.model.*
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import no.nb.bikube.hugin.model.dbo.HuginTitle
import no.nb.bikube.hugin.model.dbo.TitleIdMapping
import no.nb.bikube.hugin.repository.TitleIdMappingRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.ApplicationArguments
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionCallback
import org.springframework.transaction.support.TransactionTemplate
import reactor.core.publisher.Mono

class TitleIdMigrationRunnerTest {

    private val collectionsService = mockk<CollectionsService>()
    private val lrefConfig = CollectionsLrefConfig(
        mavisId = "2792",
        argang = "100",
        avisnr = "101",
        versjon = "102",
        originalTittel = "200",
        alternativTittel = "201",
        mediumTekst = "300",
        submediumAviser = "301",
        itemStatusPliktavlevert = "400"
    )
    private val titleRepository = mockk<TitleRepository>()
    private val titleIdMappingRepository = mockk<TitleIdMappingRepository>()
    private val jdbcTemplate = mockk<JdbcTemplate>(relaxed = true)
    private val transactionTemplate = mockk<TransactionTemplate>()
    private val args = mockk<ApplicationArguments>()

    private val runner = TitleIdMigrationRunner(
        collectionsService = collectionsService,
        lrefConfig = lrefConfig,
        titleRepository = titleRepository,
        titleIdMappingRepository = titleIdMappingRepository,
        jdbcTemplate = jdbcTemplate,
        transactionTemplate = transactionTemplate,
    )

    @BeforeEach
    fun setUp() {
        every { args.containsOption("dry-run") } returns false
        every { transactionTemplate.execute<Any?>(any()) } answers {
            firstArg<TransactionCallback<Any?>>().doInTransaction(mockk())
        }
        every { titleIdMappingRepository.saveAll(any<List<TitleIdMapping>>()) } returns emptyList()
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private fun workModel(priref: String, mavisId: String) = CollectionsModel(
        adlibJson = CollectionsRecordList(
            recordList = listOf(collectionsObject(priref, mavisId))
        )
    )

    private fun collectionsObject(priref: String, mavisId: String? = null) = CollectionsObject(
        priRef = priref,
        titleList = null,
        recordTypeList = null,
        formatList = null,
        partOfList = null,
        subMediumList = null,
        mediumList = null,
        datingList = null,
        publisherList = null,
        languageList = null,
        placeOfPublicationList = null,
        partsList = null,
        alternativeNumberList = mavisId?.let {
            listOf(CollectionsAlternativeNumber(type = "Mavis ID", value = it))
        }
    )

    private fun emptyModel() = CollectionsModel(adlibJson = CollectionsRecordList(recordList = emptyList()))

    private fun huginTitle(id: Int) = HuginTitle(id = id)

    // ── tests ─────────────────────────────────────────────────────────────────

    @Nested
    inner class HappyPath {

        @BeforeEach
        fun setUpTitles() {
            every { titleRepository.findAll() } returns listOf(huginTitle(100), huginTitle(200))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(workModel("100", "M-001"))
            every { collectionsService.getAlternativeNumbers("200") } returns Mono.just(workModel("200", "M-002"))
            every { collectionsService.search(
                "alternative_number=M-001 and alternative_number_type.lref=2792",
                CollectionsDatabase.SERIES
            ) } returns Mono.just(CollectionsModel(adlibJson = CollectionsRecordList(listOf(collectionsObject("5001")))))
            every { collectionsService.search(
                "alternative_number=M-002 and alternative_number_type.lref=2792",
                CollectionsDatabase.SERIES
            ) } returns Mono.just(CollectionsModel(adlibJson = CollectionsRecordList(listOf(collectionsObject("5002")))))
        }

        @Test
        fun `saves full mapping to title_id_mapping table`() {
            runner.run(args)

            verify {
                titleIdMappingRepository.saveAll(match<List<TitleIdMapping>> { mappings ->
                    mappings.size == 2 &&
                    mappings.any { it.oldId == 100 && it.newId == 5001 } &&
                    mappings.any { it.oldId == 200 && it.newId == 5002 }
                })
            }
        }

        @Test
        fun `runs all five SQL statements per title`() {
            runner.run(args)

            // 5 statements × 2 titles = 10 total
            verify(exactly = 10) { jdbcTemplate.update(any<String>(), *anyVararg()) }
        }

        @Test
        fun `executes updates inside a single transaction`() {
            runner.run(args)

            verify(exactly = 1) { transactionTemplate.execute<Any?>(any()) }
        }
    }

    @Nested
    inner class DryRun {

        @BeforeEach
        fun setUpTitles() {
            every { args.containsOption("dry-run") } returns true
            every { titleRepository.findAll() } returns listOf(huginTitle(100))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(workModel("100", "M-001"))
            every { collectionsService.search(any(), CollectionsDatabase.SERIES) } returns
                Mono.just(CollectionsModel(adlibJson = CollectionsRecordList(listOf(collectionsObject("5001")))))
        }

        @Test
        fun `persists mapping even in dry-run`() {
            runner.run(args)

            verify { titleIdMappingRepository.saveAll(any<List<TitleIdMapping>>()) }
        }

        @Test
        fun `does not touch DB tables in dry-run`() {
            runner.run(args)

            verify { transactionTemplate wasNot Called }
            verify { jdbcTemplate wasNot Called }
        }
    }

    @Nested
    inner class WorkLookupFailures {

        @Test
        fun `aborts when WORK not found in Collections`() {
            every { titleRepository.findAll() } returns listOf(huginTitle(999))
            every { collectionsService.getAlternativeNumbers("999") } returns Mono.just(emptyModel())

            runner.run(args)

            verify { titleIdMappingRepository wasNot Called }
            verify { jdbcTemplate wasNot Called }
        }

        @Test
        fun `aborts when WORK has no Mavis ID alternative number`() {
            every { titleRepository.findAll() } returns listOf(huginTitle(100))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(
                CollectionsModel(adlibJson = CollectionsRecordList(listOf(collectionsObject("100", null))))
            )

            runner.run(args)

            verify { titleIdMappingRepository wasNot Called }
            verify { jdbcTemplate wasNot Called }
        }
    }

    @Nested
    inner class SeriesLookupFailures {

        @Test
        fun `aborts when no Series found for Mavis ID`() {
            every { titleRepository.findAll() } returns listOf(huginTitle(100))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(workModel("100", "M-001"))
            every { collectionsService.search(any(), CollectionsDatabase.SERIES) } returns Mono.just(emptyModel())

            runner.run(args)

            verify { titleIdMappingRepository wasNot Called }
            verify { jdbcTemplate wasNot Called }
        }

        @Test
        fun `aborts only the unresolved titles and aborts all when any title fails`() {
            every { titleRepository.findAll() } returns listOf(huginTitle(100), huginTitle(200))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(workModel("100", "M-001"))
            every { collectionsService.getAlternativeNumbers("200") } returns Mono.just(emptyModel())
            every { collectionsService.search(any(), CollectionsDatabase.SERIES) } returns
                Mono.just(CollectionsModel(adlibJson = CollectionsRecordList(listOf(collectionsObject("5001")))))

            runner.run(args)

            verify { titleIdMappingRepository wasNot Called }
            verify { jdbcTemplate wasNot Called }
        }
    }

    @Nested
    inner class MultipleSeriesFound {

        @Test
        fun `uses first result when multiple Series match the Mavis ID`() {
            every { titleRepository.findAll() } returns listOf(huginTitle(100))
            every { collectionsService.getAlternativeNumbers("100") } returns Mono.just(workModel("100", "M-001"))
            every { collectionsService.search(any(), CollectionsDatabase.SERIES) } returns Mono.just(
                CollectionsModel(adlibJson = CollectionsRecordList(listOf(
                    collectionsObject("5001"),
                    collectionsObject("5099"),
                )))
            )

            runner.run(args)

            verify {
                titleIdMappingRepository.saveAll(match<List<TitleIdMapping>> { mappings ->
                    mappings.single().newId == 5001
                })
            }
        }
    }
}
