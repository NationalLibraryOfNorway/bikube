package no.nb.bikube.hugin.repository

import no.nb.bikube.hugin.model.ContactInfo
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ContactInfoRepository : JpaRepository<ContactInfo, UUID>
