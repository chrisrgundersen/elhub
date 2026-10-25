package no.gundersen.elhub.service

import no.gundersen.elhub.dto.CreateMeterReadSeriesRequest
import org.springframework.transaction.annotation.Transactional
import no.gundersen.elhub.repository.MeteringPointRepository
import no.gundersen.elhub.dto.MeterReadResponse
import no.gundersen.elhub.dto.MeterReadSumResponse
import no.gundersen.elhub.exception.MeteringPointNotFoundException
import no.gundersen.elhub.dto.toResponse
import no.gundersen.elhub.exception.DuplicateInRequestException
import no.gundersen.elhub.exception.InvalidMeteringReadingException
import no.gundersen.elhub.model.MeterReadModel
import no.gundersen.elhub.repository.MeterReadRepository


import org.springframework.stereotype.Service
import java.time.Instant


@Service
class MeterReadService(
    private val meterReadRepository: MeterReadRepository,
    private val meteringPointRepository: MeteringPointRepository,
    private val validator: MeterValidator
) {
    @Transactional
    fun create(gsrn: String, request: CreateMeterReadSeriesRequest): List<MeterReadResponse> {

        val meteringPoint = meteringPointRepository.findByGsrn(gsrn) ?: throw MeteringPointNotFoundException(gsrn)
        if (validator.hasInvalidPeriod(request.readings)) throw InvalidMeteringReadingException(gsrn)
        if (validator.hasDuplicatesInRequest(request.readings)) throw DuplicateInRequestException(gsrn)

        val meterReads = request.readings.map { reading ->
            MeterReadModel(
                meteringPoint = meteringPoint,
                periodStart = reading.periodStart,
                quantityKwh = reading.quantityKwh
            )
        }

        return meterReadRepository.saveAll(meterReads).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getReadings(gsrn: String, from: Instant, to: Instant): List<MeterReadResponse> {

        val meteringPoint = meteringPointRepository.findByGsrn(gsrn) ?: throw MeteringPointNotFoundException(gsrn)
        val meterData = meterReadRepository.findInPeriod(meteringPoint, from, to).map { it.toResponse() }
        return meterData
    }

    @Transactional(readOnly = true)
    fun getConsumption(gsrn: String, from: Instant, to: Instant): MeterReadSumResponse {
        val meteringPoint = meteringPointRepository.findByGsrn(gsrn) ?: throw MeteringPointNotFoundException(gsrn)

        return MeterReadSumResponse(
            gsrn = gsrn,
            from = from,
            to = to,
            quantityKwh = meterReadRepository.sumInPeriod(meteringPoint, from, to)
        )
    }
}