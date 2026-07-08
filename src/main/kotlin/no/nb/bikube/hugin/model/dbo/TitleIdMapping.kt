package no.nb.bikube.hugin.model.dbo

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "title_id_mapping", schema = "hugin")
data class TitleIdMapping(
    @Id
    @Column(name = "old_id", nullable = false)
    val oldId: Int,

    @Column(name = "new_id", nullable = false)
    val newId: Int,

    @Column(name = "migrated_at", nullable = false)
    val migratedAt: LocalDateTime = LocalDateTime.now(),
)
