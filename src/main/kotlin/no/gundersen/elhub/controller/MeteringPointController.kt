package no.gundersen.elhub.controller

import jakarta.validation.Valid
import no.gundersen.elhub.dto.CreateMeteringPointRequest
import no.gundersen.elhub.dto.MeteringPointResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import no.gundersen.elhub.service.MeteringPointService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@RestController
@RequestMapping("/metering-points")
class MeteringPointController (
    private val meteringPointService: MeteringPointService
){

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateMeteringPointRequest): MeteringPointResponse =
        meteringPointService.create(request)

    @GetMapping
    fun getAll(): List<MeteringPointResponse> =
        meteringPointService.getAll()
}