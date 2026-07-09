package no.nb.bikube.hugin.controller

import no.nb.bikube.api.core.model.Item
import no.nb.bikube.api.newspaper.service.NewspaperService
import no.nb.bikube.configuration.PermissiveSecurityConfig
import no.nb.bikube.hugin.model.ContactType
import no.nb.bikube.hugin.model.dbo.Box
import no.nb.bikube.hugin.model.dbo.HuginTitle
import no.nb.bikube.hugin.model.dbo.Newspaper
import no.nb.bikube.hugin.model.dto.ContactInfoDto
import no.nb.bikube.hugin.model.dto.ContactUpdateDto
import no.nb.bikube.hugin.model.dto.CreateBoxDto
import no.nb.bikube.hugin.model.dto.NewspaperUpsertDto
import no.nb.bikube.hugin.repository.BoxRepository
import no.nb.bikube.hugin.repository.NewspaperRepository
import no.nb.bikube.hugin.repository.TitleRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.Optional

@WebFluxTest(HuginNewspaperController::class)
@Import(PermissiveSecurityConfig::class)
class HuginNewspaperControllerTest {

    @Autowired
    lateinit var applicationContext: ApplicationContext

    lateinit var client: WebTestClient

    @MockitoBean
    lateinit var clientRegistrationRepository: ReactiveClientRegistrationRepository

    @MockitoBean
    lateinit var titleRepository: TitleRepository

    @MockitoBean
    lateinit var boxRepository: BoxRepository

    @MockitoBean
    lateinit var newspaperRepository: NewspaperRepository

    @MockitoBean
    lateinit var newspaperService: NewspaperService

    @BeforeEach
    fun setup() {
        client = WebTestClient
            .bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    @Test
    fun `GET titles returns 404 when title does not exist`() {
        whenever(titleRepository.findById(99)).thenReturn(Optional.empty())

        client.get().uri("/api/hugin/titles/99")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `GET titles returns the title when it exists`() {
        val title = HuginTitle(id = 123, vendor = "Acme", shelf = "A-1")
        whenever(titleRepository.findById(123)).thenReturn(Optional.of(title))

        client.get().uri("/api/hugin/titles/123")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(123)
            .jsonPath("$.vendor").isEqualTo("Acme")
            .jsonPath("$.shelf").isEqualTo("A-1")
    }

    @Test
    fun `POST box deactivates existing boxes and creates a new active box`() {
        val title = HuginTitle(id = 1)
        val existingBox = Box(id = "old-box", dateFrom = LocalDate.of(2023, 1, 1), active = true, title = title)
        val newBox = Box(id = "new-box", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = title)

        whenever(titleRepository.findById(1)).thenReturn(Optional.of(title))
        whenever(boxRepository.findAllByTitleIdOrderByDateFromAsc(1)).thenReturn(listOf(existingBox))
        whenever(boxRepository.saveAll(any<List<Box>>())).thenReturn(listOf(existingBox))
        whenever(boxRepository.save(any())).thenReturn(newBox)

        client.post().uri("/api/hugin/box")
            .bodyValue(CreateBoxDto(titleId = 1, id = "new-box", dateFrom = LocalDate.of(2024, 1, 1)))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("new-box")

        assertFalse(existingBox.active)
        verify(boxRepository).saveAll(any<List<Box>>())
    }

    @Test
    fun `POST box creates the first active box when title has none yet`() {
        val title = HuginTitle(id = 2)
        val newBox = Box(id = "first-box", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = title)

        whenever(titleRepository.findById(2)).thenReturn(Optional.of(title))
        whenever(boxRepository.findAllByTitleIdOrderByDateFromAsc(2)).thenReturn(emptyList())
        whenever(boxRepository.saveAll(any<List<Box>>())).thenReturn(emptyList())
        whenever(boxRepository.save(any())).thenReturn(newBox)

        client.post().uri("/api/hugin/box")
            .bodyValue(CreateBoxDto(titleId = 2, id = "first-box", dateFrom = LocalDate.of(2024, 1, 1)))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo("first-box")

        verify(boxRepository).saveAll(emptyList())
    }

    @Test
    fun `PUT titles contact creates new title when it does not exist`() {
        whenever(titleRepository.findById(42)).thenReturn(Optional.empty())
        whenever(titleRepository.save(any())).thenAnswer { it.arguments[0] }

        val dto = ContactUpdateDto(
            id = 42,
            vendor = "Acme",
            contactInfos = listOf(ContactInfoDto(contactType = ContactType.phone, contactValue = "12345678")),
        )

        client.put().uri("/api/hugin/titles/contact")
            .bodyValue(dto)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(42)
            .jsonPath("$.vendor").isEqualTo("Acme")
    }

    @Test
    fun `POST newspapers batch creates a missing item when not received`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val createdItem = Item(
            catalogueId = "cat1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "cat1",
        )
        val savedNewspaper = Newspaper(
            catalogId = "cat1", box = box, date = LocalDate.of(2024, 1, 2), edition = null, received = false, notes = null,
        )

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.createMissingItem(any())).thenReturn(Mono.just(createdItem))
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = false)

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].catalogId").isEqualTo("cat1")
    }

    @Test
    fun `POST newspapers batch creates a physical item when received and no catalogId given`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val createdItem = Item(
            catalogueId = "item1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "manifest1",
        )
        val savedNewspaper = Newspaper(
            catalogId = "manifest1", box = box, date = LocalDate.of(2024, 1, 2), edition = null, received = true, notes = null,
        )

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.createNewspaperItem(any())).thenReturn(Mono.just(createdItem))
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = true)

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].catalogId").isEqualTo("manifest1")

        verify(newspaperService).createNewspaperItem(any())
    }

    @Test
    fun `POST newspapers batch updates manifestation when received status is unchanged`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val catalogueItem = Item(
            catalogueId = "item1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "manifest1",
        )
        val existingNewspaper = Newspaper(
            catalogId = "manifest1", box = box, date = LocalDate.of(2024, 1, 2), edition = "1", received = true, notes = null,
        )
        val savedNewspaper = existingNewspaper.copy(notes = "Updated")

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.getSingleManifestationAsItem("item1")).thenReturn(Mono.just(catalogueItem))
        whenever(newspaperRepository.findById("manifest1")).thenReturn(Optional.of(existingNewspaper))
        whenever(newspaperService.updatePhysicalNewspaper(any())).thenReturn(Mono.empty())
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(
            titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = true, catalogId = "item1",
        )

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk

        verify(newspaperService).updatePhysicalNewspaper(any())
        verify(newspaperService, never()).createNewspaperItem(any())
        verify(newspaperService, never()).deletePhysicalItemByManifestationId(any(), any())
    }

    @Test
    fun `POST newspapers batch creates physical item when existing manifestation was not received`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val catalogueItem = Item(
            catalogueId = "item1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "manifest1",
        )
        val existingNewspaper = Newspaper(
            catalogId = "manifest1", box = box, date = LocalDate.of(2024, 1, 2), edition = "1", received = false, notes = null,
        )
        val createdItem = catalogueItem.copy(catalogueId = "item2")
        val savedNewspaper = existingNewspaper.copy(received = true)

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.getSingleManifestationAsItem("item1")).thenReturn(Mono.just(catalogueItem))
        whenever(newspaperRepository.findById("manifest1")).thenReturn(Optional.of(existingNewspaper))
        whenever(newspaperService.createNewspaperItem(any())).thenReturn(Mono.just(createdItem))
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(
            titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = true, catalogId = "item1",
        )

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk

        verify(newspaperService).createNewspaperItem(any())
        verify(newspaperService, never()).updatePhysicalNewspaper(any())
        verify(newspaperService, never()).deletePhysicalItemByManifestationId(any(), any())
    }

    @Test
    fun `POST newspapers batch deletes physical item when existing manifestation becomes unreceived`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val catalogueItem = Item(
            catalogueId = "item1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "manifest1",
        )
        val existingNewspaper = Newspaper(
            catalogId = "manifest1", box = box, date = LocalDate.of(2024, 1, 2), edition = "1", received = true, notes = null,
        )
        val savedNewspaper = existingNewspaper.copy(received = false)

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.getSingleManifestationAsItem("item1")).thenReturn(Mono.just(catalogueItem))
        whenever(newspaperRepository.findById("manifest1")).thenReturn(Optional.of(existingNewspaper))
        whenever(newspaperService.deletePhysicalItemByManifestationId("manifest1", false)).thenReturn(Mono.empty())
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(
            titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = false, catalogId = "item1",
        )

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk

        verify(newspaperService).deletePhysicalItemByManifestationId("manifest1", false)
        verify(newspaperService, never()).createNewspaperItem(any())
        verify(newspaperService, never()).updatePhysicalNewspaper(any())
    }

    @Test
    fun `POST newspapers batch saves without calling collections service when manifestation is not tracked locally`() {
        val box = Box(id = "box1", dateFrom = LocalDate.of(2024, 1, 1), active = true, title = HuginTitle(id = 1))
        val catalogueItem = Item(
            catalogueId = "item1", name = null, date = LocalDate.of(2024, 1, 2), materialType = "NEWSPAPER",
            titleCatalogueId = "1", titleName = null, digital = false, urn = null, parentCatalogueId = "manifest1",
        )
        val savedNewspaper = Newspaper(
            catalogId = "item1", box = box, date = LocalDate.of(2024, 1, 2), edition = null, received = true, notes = null,
        )

        whenever(boxRepository.findById("box1")).thenReturn(Optional.of(box))
        whenever(newspaperService.getSingleManifestationAsItem("item1")).thenReturn(Mono.just(catalogueItem))
        whenever(newspaperRepository.findById("manifest1")).thenReturn(Optional.empty())
        whenever(newspaperRepository.findById("item1")).thenReturn(Optional.empty())
        whenever(newspaperRepository.save(any())).thenReturn(savedNewspaper)

        val upsert = NewspaperUpsertDto(
            titleId = 1, boxId = "box1", date = LocalDate.of(2024, 1, 2), received = true, catalogId = "item1",
        )

        client.mutateWith(mockOidcLogin().idToken { it.claim("preferred_username", "testuser") })
            .post().uri("/api/hugin/newspapers/batch")
            .bodyValue(listOf(upsert))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[0].catalogId").isEqualTo("item1")

        verify(newspaperService, never()).createNewspaperItem(any())
        verify(newspaperService, never()).updatePhysicalNewspaper(any())
        verify(newspaperService, never()).deletePhysicalItemByManifestationId(any(), any())
    }

    @Test
    fun `DELETE newspapers deletes existing newspaper and returns 204`() {
        whenever(newspaperService.deletePhysicalItemByManifestationId("man1", true)).thenReturn(Mono.empty())
        whenever(newspaperRepository.existsById("man1")).thenReturn(true)

        client.delete().uri("/api/hugin/newspapers/man1")
            .exchange()
            .expectStatus().isNoContent

        verify(newspaperRepository).deleteById("man1")
    }
}
