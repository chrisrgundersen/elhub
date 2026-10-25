package no.gundersen.elhub.service

import no.gundersen.elhub.dto.CreateMeterReadingRequest
import org.springframework.stereotype.Component
import java.time.temporal.ChronoUnit

@Component
class MeterValidator {

    //Improvement for debugging - include which periods that are invalid
    fun hasInvalidPeriod(meterData: List<CreateMeterReadingRequest>) : Boolean {
        val periods = meterData.map {it.periodStart}
        for (period in periods) {
            if (period != period.truncatedTo(ChronoUnit.HOURS)) return true
        }
        return false
    }
    //Improvement for debugging - include which periods that are duplicates
    fun hasDuplicatesInRequest(meterData: List<CreateMeterReadingRequest>) : Boolean {
        val periods = meterData.map {it.periodStart}
        val duplicates = periods.size != periods.toSet().size
        return duplicates
    }
}