package no.gundersen.elhub.controller

import jakarta.validation.Valid
import no.gundersen.elhub.dto.CreateMeterReadSeriesRequest
import no.gundersen.elhub.dto.MeterReadResponse
import no.gundersen.elhub.dto.MeterReadSumResponse
import no.gundersen.elhub.service.MeterReadService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/metering-points/{gsrn}")
class MeterReadController (
    private val meterReadService: MeterReadService
) {

    @PostMapping("/readings")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@PathVariable gsrn: String, @Valid @RequestBody request: CreateMeterReadSeriesRequest): List<MeterReadResponse> =
        meterReadService.create(gsrn, request)

    @GetMapping( "/readings")
    fun getReadings(@PathVariable gsrn: String,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant
    ): List<MeterReadResponse> = meterReadService.getReadings(gsrn, from, to)

    @GetMapping( "/consumption")
    fun getConsumption(@PathVariable gsrn: String,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant
    ): MeterReadSumResponse = meterReadService.getConsumption(gsrn, from, to)
}