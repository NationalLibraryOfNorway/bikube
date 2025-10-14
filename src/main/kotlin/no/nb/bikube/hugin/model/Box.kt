package no.nb.bikube.hugin.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "box", schema = "hugin")
data class Box(
    @Id
    @Column(name = "id", nullable = false)
    var id: String = "",

    @Column(name = "date_from", nullable = false)
    var dateFrom: LocalDate? = null,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name = "title_id", nullable = false)
    @JsonBackReference("title-boxes")
    var title: HuginTitle? = null,

    @OneToMany(mappedBy = "box", cascade = [], orphanRemoval = false, fetch = FetchType.LAZY)
    @JsonManagedReference("box-newspapers")
    var newspapers: MutableList<Newspaper> = mutableListOf(),
)

