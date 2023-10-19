package no.nb.bikube.core.service

import no.nb.bikube.core.exception.BadRequestBodyException
import no.nb.bikube.core.model.inputDto.ItemInputDto
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class CreationValidationServiceTest {
    @Autowired
    private lateinit var creationValidationService: CreationValidationService

    private val validDigitalItem = ItemInputDto(
        date = LocalDate.parse("2020-01-01"),
        titleCatalogueId = "1",
        digital = true,
        urn = "avis_null_null_20200101_1_1_1"
    )

    private val validPhysicalItem = ItemInputDto(
        date = LocalDate.parse("2020-01-01"),
        titleCatalogueId = "1",
        digital = false,
        urn = null
    )

    @Test
    fun `validateItem should allow valid items`() {
        Assertions.assertDoesNotThrow{ creationValidationService.validateItem(validDigitalItem) }
        Assertions.assertDoesNotThrow{ creationValidationService.validateItem(validPhysicalItem) }
    }

    @Test
    fun `validateItem should throw exception if titleCatalogueId is null or blank`() {
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validPhysicalItem.copy(titleCatalogueId = null)) }
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validDigitalItem.copy(titleCatalogueId = "")) }
    }

    @Test
    fun `validateItem should throw exception if date is null`() {
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validPhysicalItem.copy(date = null)) }
    }

    @Test
    fun `validateItem should throw exception if digital is null`() {
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validPhysicalItem.copy(digital = null)) }
    }

    @Test
    fun `validateItem should throw exception if digital is true and urn is null or blank`() {
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validDigitalItem.copy(urn = null)) }
        assertThrows<BadRequestBodyException> { creationValidationService.validateItem(validDigitalItem.copy(urn = "")) }
    }
}
