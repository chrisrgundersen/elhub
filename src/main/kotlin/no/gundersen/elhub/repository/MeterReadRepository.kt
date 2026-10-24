package no.gundersen.elhub.repository

import no.gundersen.elhub.model.MeterReadModel
import no.gundersen.elhub.model.MeteringPointModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.Instant

interface MeterReadRepository : JpaRepository<MeterReadModel, Long> {

    @Query("""
        select r from MeterReadModel r
        where r.meteringPoint = :meteringPoint
            and r.periodStart >= :from
            and r.periodStart < :to
            order by r.periodStart
    """)
    fun findInPeriod(
        @Param("meteringPoint") meteringPoint: MeteringPointModel,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ) : List<MeterReadModel>

    @Query("""
    SELECT COALESCE(SUM(r.quantityKwh), 0)
    FROM MeterReadModel r
    WHERE r.meteringPoint = :meteringPoint
      AND r.periodStart >= :from
      AND r.periodStart < :to
""")
    fun sumInPeriod(
        @Param("meteringPoint") meteringPoint: MeteringPointModel,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): BigDecimal
}