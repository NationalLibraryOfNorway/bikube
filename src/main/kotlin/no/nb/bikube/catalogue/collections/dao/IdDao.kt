package no.nb.bikube.catalogue.collections.dao

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository

interface IdDao: Repository<String, String> {

    @Query("SELECT nextval('collections_id_seq')", nativeQuery = true)
    fun getUniqueId(): String

}