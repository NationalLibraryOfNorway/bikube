package no.nb.bikube.hugin.migration

import no.nb.bikube.api.catalogue.collections.config.CollectionsLrefConfig
import no.nb.bikube.api.catalogue.collections.enum.CollectionsDatabase
import no.nb.bikube.api.catalogue.collections.service.CollectionsService
import no.nb.bikube.api.core.util.logger
import no.nb.bikube.hugin.model.dbo.TitleIdMapping
import no.nb.bikube.hugin.repository.TitleIdMappingRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

/**
 * One-time migration: updates hugin.title.id (and box.title_id FK) from old WORK
 * prirefs to new SERIES prirefs.
 *
 * Lookup strategy per title:
 *   1. Fetch the WORK record from Collections by the existing HuginTitle.id as priref.
 *   2. Extract the Mavis ID from the WORK's alternative_number list (type lref = mavisId).
 *   3. Search Collections SERIES for a record with that same Mavis ID alternative_number.
 *   4. Use the resulting SERIES priref as the new HuginTitle.id.
 *
 * Run with: --spring.profiles.active=hugin-migration
 * Add --dry-run to preview the mapping without writing to the DB.
 */
@Component
@Profile("hugin-migration")
class TitleIdMigrationRunner(
    @Qualifier("collectionsNewspaperService")
    private val collectionsService: CollectionsService,
    private val lrefConfig: CollectionsLrefConfig,
    private val titleRepository: TitleRepository,
    private val titleIdMappingRepository: TitleIdMappingRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val transactionTemplate: TransactionTemplate,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        val dryRun = args.containsOption("dry-run")
        val titles = titleRepository.findAll()
        logger().info("Hugin title ID migration: {} titles found (dry-run={})", titles.size, dryRun)

        val mapping = mutableMapOf<Int, Int>()
        val failed = mutableListOf<Int>()

        titles.forEach { title ->
            val newPriref = resolveNewSeriesPriref(title.id)
            if (newPriref != null) {
                logger().info("  Resolved: {} -> {}", title.id, newPriref)
                mapping[title.id] = newPriref
            } else {
                failed.add(title.id)
            }
        }

        if (failed.isNotEmpty()) {
            logger().error("Could not resolve new Series priref for title IDs: {}", failed)
            logger().error("Aborting — fix the {} unresolved titles before re-running", failed.size)
            return
        }

        val mappingRecords = mapping.map { (oldId, newId) -> TitleIdMapping(oldId = oldId, newId = newId) }
        titleIdMappingRepository.saveAll(mappingRecords)
        logger().info("Mapping saved to hugin.title_id_mapping")

        if (dryRun) {
            logger().info("Dry run complete — mapping persisted but title IDs not updated")
            return
        }

        transactionTemplate.execute {
            mapping.forEach { (oldId, newId) -> migrateTitle(oldId, newId) }
        }
        logger().info("Migration complete — {} titles updated", mapping.size)
    }

    private fun resolveNewSeriesPriref(workPriref: Int): Int? {
        val mavisId = extractMavisIdFromWork(workPriref) ?: return null
        return findSeriesPrirefByMavisId(mavisId, workPriref)
    }

    private fun extractMavisIdFromWork(workPriref: Int): String? {
        val model = collectionsService.getAlternativeNumbers(workPriref.toString()).block()
        val work = model?.getObjects()?.firstOrNull()
        if (work == null) {
            logger().error("No WORK found in Collections for priref {}", workPriref)
            return null
        }
        val mavisId = work.alternativeNumberList
            ?.find { it.type == "Mavis ID" }
            ?.value
        if (mavisId == null) {
            logger().error("No Mavis ID alternative_number found on WORK priref {}", workPriref)
        }
        return mavisId
    }

    private fun findSeriesPrirefByMavisId(mavisId: String, workPriref: Int): Int? {
        val query = "alternative_number=$mavisId and alternative_number_type.lref=${lrefConfig.mavisId}"
        val result = collectionsService.search(query, CollectionsDatabase.SERIES).block()
        val objects = result?.getObjects()
        return when {
            objects.isNullOrEmpty() -> {
                logger().error("No Series found for Mavis ID {} (work priref {})", mavisId, workPriref)
                null
            }
            objects.size > 1 -> {
                logger().warn("Multiple Series for Mavis ID {}, using priref={}", mavisId, objects.first().priRef)
                objects.first().priRef.toIntOrNull().also {
                    if (it == null) logger().error("Series priRef '{}' is not an integer", objects.first().priRef)
                }
            }
            else -> objects.first().priRef.toIntOrNull().also {
                if (it == null) logger().error("Series priRef '{}' is not an integer for Mavis ID {}", objects.first().priRef, mavisId)
            }
        }
    }

    private fun migrateTitle(oldId: Int, newId: Int) {
        logger().info("Migrating hugin.title {} -> {}", oldId, newId)

        // Insert new title row (copy of the old one with the new PK)
        jdbcTemplate.update("""
            INSERT INTO hugin.title (id, contact_name, vendor, release_pattern, shelf, notes)
            SELECT ?, contact_name, vendor, release_pattern, shelf, notes
            FROM hugin.title WHERE id = ?
        """.trimIndent(), newId, oldId)

        // Copy contact_info rows to reference the new title (new UUID per row)
        jdbcTemplate.update("""
            INSERT INTO hugin.contact_info (id, title_id, contact_type, contact_value)
            SELECT gen_random_uuid(), ?, contact_type, contact_value
            FROM hugin.contact_info WHERE title_id = ?
        """.trimIndent(), newId, oldId)

        // Re-parent all boxes
        jdbcTemplate.update(
            "UPDATE hugin.box SET title_id = ? WHERE title_id = ?",
            newId, oldId
        )

        // Remove old contact_info rows
        jdbcTemplate.update("DELETE FROM hugin.contact_info WHERE title_id = ?", oldId)

        // Remove old title row
        jdbcTemplate.update("DELETE FROM hugin.title WHERE id = ?", oldId)
    }
}
