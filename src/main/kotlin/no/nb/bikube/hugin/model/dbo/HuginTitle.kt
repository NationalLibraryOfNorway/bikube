package no.nb.bikube.hugin.model.dbo

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "title", schema = "hugin")
data class HuginTitle(
    @Id
    @Column(name = "id", nullable = false)
    var id: Int = 0,

    @Column(name = "contact_name")
    var contactName: String? = null,

    @Column(name = "vendor")
    var vendor: String? = null,

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "release_pattern", columnDefinition = "integer[]", nullable = true)
    var releasePattern: Array<Int>? = arrayOf(0, 0, 0, 0, 0, 0, 0),

    @Column(name = "shelf")
    var shelf: String? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @OneToMany(mappedBy = "title", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference("title-boxes")
    var boxes: MutableList<Box> = mutableListOf(),

    @OneToMany(mappedBy = "title", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference("title-contactInfos")
    var contactInfos: MutableList<ContactInfo> = mutableListOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HuginTitle

        if (id != other.id) return false
        if (contactName != other.contactName) return false
        if (vendor != other.vendor) return false
        if (!releasePattern.contentEquals(other.releasePattern)) return false
        if (shelf != other.shelf) return false
        if (notes != other.notes) return false
        if (boxes != other.boxes) return false
        if (contactInfos != other.contactInfos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (contactName?.hashCode() ?: 0)
        result = 31 * result + (vendor?.hashCode() ?: 0)
        result = 31 * result + (releasePattern?.contentHashCode() ?: 0)
        result = 31 * result + (shelf?.hashCode() ?: 0)
        result = 31 * result + (notes?.hashCode() ?: 0)
        result = 31 * result + boxes.hashCode()
        result = 31 * result + contactInfos.hashCode()
        return result
    }
}
