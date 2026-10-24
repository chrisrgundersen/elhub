package no.gundersen.elhub.repository

import no.gundersen.elhub.model.MeteringPointModel
import org.springframework.data.jpa.repository.JpaRepository

interface MeteringPointRepository : JpaRepository<MeteringPointModel, Long> {
    fun existsByGsrn(gsrn: String): Boolean
}