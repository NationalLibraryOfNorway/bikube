package no.nb.bikube.newspaper.service

import jakarta.persistence.EntityManager
import no.nb.bikube.core.util.logger
import org.springframework.stereotype.Service

@Service
class UniqueIdService(
    private val entityManager: EntityManager
) {
    fun getUniqueId(): String {
        val query = entityManager.createNativeQuery("SELECT nextval('maxit.collections_id_seq')")
        val id = query.singleResult.toString()
        logger().info("Got unique id $id from database sequence")
        return id
    }
}