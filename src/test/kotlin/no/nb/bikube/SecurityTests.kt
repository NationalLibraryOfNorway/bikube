package no.nb.bikube

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nb.bikube.api.core.enum.MaterialType
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockA
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperItemMockCValidForCreation
import no.nb.bikube.api.newspaper.NewspaperMockData.Companion.newspaperTitleMockA
import no.nb.bikube.api.newspaper.service.NewspaperService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource( properties = [
    "server.servlet.context-path=/bikube",
    "security.enabled=true"
])
class SecurityTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val jsonMapper: JsonMapper
) {
    @MockkBean
    private lateinit var newspaperService: NewspaperService

    @Test
    fun `should allow access to get endpoints without login`() {
        every { newspaperService.getSingleItem(any()) } returns Mono.just(newspaperItemMockA)

        mockMvc.perform(
            get("/api/item")
                .param("catalogueId", "123")
                .param("materialType", MaterialType.NEWSPAPER.name)
        ).andExpect(status().isOk)
    }

    @Test
    fun `should allow access to get api docs without login`() {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk)
    }

    @Test
    fun `should not allow access to post endpoints without login`() {
        mockMvc.perform(
            post("/api/newspapers/items")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `should allow access to post endpoints when logged in`() {
        every { newspaperService.getSingleTitle(any()) } returns Mono.just(newspaperTitleMockA)
        every { newspaperService.createNewspaperItem(any()) } returns Mono.just(newspaperItemMockA)

        mockMvc.perform(
            post("/api/newspapers/items")
                .with(jwt().authorities(SimpleGrantedAuthority("bikube-create")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
        ).andExpect(status().isOk)
    }

    @Test
    fun `should return 401 on post for invalid token`() {
        mockMvc.perform(
            post("/api/newspapers/items")
                .header("Authorization", "Bearer eyIkkeEnToken")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsBytes(newspaperItemMockCValidForCreation))
        ).andExpect(status().isUnauthorized)
    }
}
