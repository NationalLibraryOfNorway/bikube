package no.nb.bikube.hugin.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "newspaper", schema = "hugin")
class Newspaper() {

    @Column(name = "edition")
    var edition: String? = null;

    @Column(name = "date")
    var date: LocalDate? = null;

    @Column(name = "received")
    var received: Boolean? = null;

    @Column(name = "username")
    var username: String? = null;

    @Column(name = "notes")
    var notes: String? = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    @JsonBackReference("box-newspapers")
    var box: Box? = null;

    @Id
    @Column(name = "catalog_id", nullable = false)
    var catalogId: String = "";
}
