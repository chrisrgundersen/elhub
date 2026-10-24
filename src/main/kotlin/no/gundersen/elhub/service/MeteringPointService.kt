package no.gundersen.elhub.service

import org.springframework.transaction.annotation.Transactional
import no.gundersen.elhub.dto.MeteringPointResponse
import no.gundersen.elhub.repository.MeteringPointRepository
import no.gundersen.elhub.dto.CreateMeteringPointRequest
import no.gundersen.elhub.exception.DuplicateMeteringPointException
import no.gundersen.elhub.exception.MeteringPointNotFoundException
import no.gundersen.elhub.model.MeteringPointModel
import no.gundersen.elhub.dto.toResponse
import org.springframework.data.repository.findByIdOrNull


import org.springframework.stereotype.Service


@Service
class MeteringPointService(
    private val meteringPointRepository: MeteringPointRepository
) {
    @Transactional
    fun create(request: CreateMeteringPointRequest): MeteringPointResponse {
        if (meteringPointRepository.existsByGsrn(request.gsrn)) {
            throw DuplicateMeteringPointException(request.gsrn)
        }
        val meteringPoint = MeteringPointModel(gsrn = request.gsrn, name = request.name)
        return meteringPointRepository.save(meteringPoint).toResponse()
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): MeteringPointResponse =
        meteringPointRepository.findByIdOrNull(id)?.toResponse() ?: throw MeteringPointNotFoundException(id)
}