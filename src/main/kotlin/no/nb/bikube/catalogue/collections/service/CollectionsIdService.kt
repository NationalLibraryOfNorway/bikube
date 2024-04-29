package no.nb.bikube.catalogue.collections.service

import no.nb.bikube.catalogue.collections.dao.IdDao
import org.springframework.stereotype.Service

@Service
class CollectionsIdService(
    private val idDao: IdDao
) {
    fun getUniqueId(): String {
        return idDao.getUniqueId()
    }
}