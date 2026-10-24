package no.gundersen.elhub.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "meter_reading",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_reading_point_period",
            columnNames = ["metering_point_id", "period_start"])
    ]
)
class MeterReadModel (
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metering_point_id", nullable = false)
    val meteringPoint: MeteringPointModel,

    @Column(nullable = false)
    var periodStart: Instant,

    @Column(nullable = false, precision = 14, scale = 3)
    var quantityKwh: BigDecimal,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null


}