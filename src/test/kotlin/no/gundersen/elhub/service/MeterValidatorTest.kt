package no.gundersen.elhub.service

import no.gundersen.elhub.dto.CreateMeterReadingRequest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class MeterValidatorTest {
    private val validator = MeterValidator()

    private fun generateTestData(time: String, kwh: BigDecimal): CreateMeterReadingRequest {
        return CreateMeterReadingRequest(periodStart = Instant.parse(time), quantityKwh = kwh)
    }

    @Test
    fun `Test 1 - periode hele timer ugyldig`() {
        val readings = listOf(generateTestData(time = "2026-10-25T10:00:00Z", kwh = BigDecimal(10)), generateTestData(time = "2026-10-25T10:10:00Z", kwh = BigDecimal(10)))
        assertTrue(validator.hasInvalidPeriod(readings))
    }

    @Test
    fun `Test 2 - periode hele timer gyldig`() {
        val readings = listOf(generateTestData(time = "2026-10-25T10:00:00Z", kwh = BigDecimal(10)), generateTestData(time = "2026-10-25T11:00:00Z", kwh = BigDecimal(10)))
        assertFalse(validator.hasInvalidPeriod(readings))
    }

    @Test
    fun `Test 3 - duplikater ugyldig`() {
        val readings = listOf(generateTestData(time = "2026-10-25T10:00:00Z", kwh = BigDecimal(10)), generateTestData(time = "2026-10-25T10:00:00Z", kwh = BigDecimal(10)))
        assertTrue(validator.hasDuplicatesInRequest(readings))
    }

    @Test
    fun `Test 4 - duplikater gyldig`() {
        val readings = listOf(generateTestData(time = "2026-10-25T10:00:00Z", kwh = BigDecimal(10)), generateTestData(time = "2026-10-25T11:00:00Z", kwh = BigDecimal(10)))
        assertFalse(validator.hasDuplicatesInRequest(readings))
    }
}