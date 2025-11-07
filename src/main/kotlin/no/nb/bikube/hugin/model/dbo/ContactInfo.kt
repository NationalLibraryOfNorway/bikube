package no.nb.bikube.hugin.model.dbo

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import no.nb.bikube.hugin.model.ContactType
import java.util.UUID

@Entity
@Table(
    name = "contact_info", schema = "hugin",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_contact",
            columnNames = ["title_id", "contact_type", "contact_value"]
        )
    ]
)
data class ContactInfo(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "title_id", nullable = false)
    @JsonBackReference("title-contactInfos")
    var title: HuginTitle? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type", nullable = false)
    var contactType: ContactType? = null,

    @Column(name = "contact_value")
    var contactValue: String? = null,
)
