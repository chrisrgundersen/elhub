package no.gundersen.elhub.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.gundersen.elhub.model.MeterReadModel
import java.math.BigDecimal
import java.time.Instant

data class CreateMeterReadSeriesRequest(
    @field:NotEmpty
    @field:Valid
    val readings: List<CreateMeterReadingRequest>
)

data class CreateMeterReadingRequest(
    @field:NotNull
    val periodStart: Instant,

    @field:NotNull
    @field:DecimalMin("0.000")
    val quantityKwh: BigDecimal
)

data class MeterReadResponse(
    val id: Long,
    val gsrn: String,
    val periodStart: Instant,
    val quantityKwh: BigDecimal,
    val createdAt: Instant
)

fun MeterReadModel.toResponse() = MeterReadResponse(
    id = id!!,
    gsrn = meteringPoint.gsrn,
    periodStart = periodStart,
    quantityKwh = quantityKwh,
    createdAt = createdAt
)

data class MeterReadSumResponse(
    val gsrn: String,
    val from: Instant,
    val to: Instant,
    val quantityKwh: BigDecimal
)