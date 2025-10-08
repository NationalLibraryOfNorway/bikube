package no.nb.bikube.hugin.model

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
class ContactInfo() {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    var id: UUID? = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id")
    @JsonBackReference("title-contactInfos")
    var title: Title? = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "contact_type")
    var contactType: ContactType? = null;

    @Column(name = "contact_value")
    var contactValue: String? = null;

    enum class ContactType {
        phone, email
    }
}
