package no.nb.bikube.core.service

import no.nb.bikube.catalogue.collections.CollectionsModelMockData.Companion.TEST_USERNAME
import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.model.inputDto.ItemInputDto
import no.nb.bikube.core.model.inputDto.ItemUpdateDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class DtoValidationServiceTest {
    @Autowired
    private lateinit var dtoValidationService: DtoValidationService

    private val validDigitalItem = ItemInputDto(
        date = LocalDate.parse("2020-01-01"),
        titleCatalogueId = "1",
        username = TEST_USERNAME,
        digital = true,
        urn = "avis_null_null_20200101_1_1_1",
        containerId = null
    )

    private val validPhysicalItem = ItemInputDto(
        date = LocalDate.parse("2020-01-01"),
        titleCatalogueId = "1",
        username = TEST_USERNAME,
        digital = false,
        urn = null,
        containerId = "1"
    )

    @Test
    fun `validateItemInputDto should allow valid items`() {
        Assertions.assertDoesNotThrow{ dtoValidationService.validateItemInputDto(validDigitalItem) }
        Assertions.assertDoesNotThrow{ dtoValidationService.validateItemInputDto(validPhysicalItem) }
    }

    @Test
    fun `validateItemInputDto should throw exception if titleCatalogueId is blank`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemInputDto(validDigitalItem.copy(titleCatalogueId = "")) }
    }

    @Test
    fun `validateItemInputDto should throw exception if digital is null`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemInputDto(validPhysicalItem.copy(digital = null)) }
    }

    @Test
    fun `validateItemInputDto should throw exception if digital is true and urn is null or blank`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemInputDto(validDigitalItem.copy(urn = null)) }
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemInputDto(validDigitalItem.copy(urn = "")) }
    }

    @Test
    fun `validateItemInputDto should throw exception if digital is false and containerId is null or blank`() {
        assertThrows<BadRequestBodyException> {
            dtoValidationService.validateItemInputDto(validDigitalItem.copy(digital = false, containerId = null))
        }
    }

    @Test
    fun `validateItemUpdateDto should throw exception if neither notes or number is provided`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemUpdateDto(
            ItemUpdateDto(username = TEST_USERNAME, manifestationId = "1")
        )}
    }

    @Test
    fun `validateItemUpdateDto should not throw exception if only notes or number is provided`() {
        assertDoesNotThrow { dtoValidationService.validateItemUpdateDto(
            ItemUpdateDto(username = TEST_USERNAME, manifestationId = "1", notes = "notes")
        )}

        assertDoesNotThrow { dtoValidationService.validateItemUpdateDto(
            ItemUpdateDto(username = TEST_USERNAME, manifestationId = "1", number = "1")
        )}
    }

    @Test
    fun `validateItemUpdateDto should throw exception if username is blank`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemUpdateDto(
            ItemUpdateDto(username = "", manifestationId = "1", notes = "notes")
        )}
    }

    @Test
    fun `validateItemUpdateDto should throw exception if manifestationId is blank`() {
        assertThrows<BadRequestBodyException> { dtoValidationService.validateItemUpdateDto(
            ItemUpdateDto(username = TEST_USERNAME, manifestationId = "", notes = "notes")
        )}
    }
}
