package no.nb.bikube.hugin.model.dbo

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "title", schema = "hugin")
open class Title() {

    @Id
    @Column(name = "id", nullable = false)
    var id: Int = 0

    @Column(name = "contact_name")
    var contactName: String? = null

    @Column(name = "vendor")
    var vendor: String? = null

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "release_pattern", columnDefinition = "integer[]", nullable = true)
    var releasePattern: Array<Int>? = arrayOf(0, 0, 0, 0, 0, 0, 0)

    @Column(name = "shelf")
    var shelf: String? = null

    @Column(name = "notes")
    var notes: String? = null

    @OneToMany(mappedBy = "title", fetch = FetchType.LAZY)
    @JsonManagedReference("title-boxes")
    var boxes: MutableList<Box> = mutableListOf()

    @OneToMany(mappedBy = "title", fetch = FetchType.LAZY)
    @JsonManagedReference("title-contactInfos")
    var contactInfos: MutableList<ContactInfo> = mutableListOf()
}
