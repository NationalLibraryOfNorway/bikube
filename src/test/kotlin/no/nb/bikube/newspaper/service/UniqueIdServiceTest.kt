package no.nb.bikube.newspaper.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UniqueIdServiceTest {
    @Autowired private lateinit var uniqueIdService: UniqueIdService

    @MockkBean private lateinit var entityManager: EntityManager

    @Test
    fun `should get unique id`() {
        every { entityManager.createNativeQuery("SELECT nextval('collections_id_seq')") } returns mockk {
            every { singleResult } returns 123L
        }

        val id = uniqueIdService.getUniqueId()
        assertEquals("123", id)
    }

}