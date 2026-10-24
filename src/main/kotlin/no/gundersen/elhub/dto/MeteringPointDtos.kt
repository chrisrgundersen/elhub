package no.gundersen.elhub.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import no.gundersen.elhub.model.MeteringPointModel
import java.time.Instant

data class CreateMeteringPointRequest(
    @field:NotBlank
    @field:Pattern(regexp = "\\d{18}", message = "GSRN må være 18 siffer")
    val gsrn: String,

    @field:NotBlank
    val name: String
)

data class MeteringPointResponse(
    val id: Long,
    val gsrn: String,
    val name: String,
    val createdAt: Instant
)

fun MeteringPointModel.toResponse() = MeteringPointResponse(
    id = id!!,
    gsrn = gsrn,
    name = name,
    createdAt = createdAt
)