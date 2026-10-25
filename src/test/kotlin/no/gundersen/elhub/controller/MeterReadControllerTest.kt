package no.gundersen.elhub.controller

import no.gundersen.elhub.TestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class MeterReadControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `Test 1 - Register and Consumption`() {
        val gsrn = "707057500000000010"

        // 1. Create metering-point
        mockMvc.post("/metering-points") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"gsrn":"$gsrn","name":"Integrasjonstest"}"""
        }.andExpect { status { isCreated() } }

        // 2. Register values
        mockMvc.post("/metering-points/$gsrn/readings") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"readings":[
                    {"periodStart":"2026-10-23T10:00:00Z","quantityKwh":1.250},
                    {"periodStart":"2026-10-23T11:00:00Z","quantityKwh":0.750}
                ]}
            """
        }.andExpect { status { isCreated() } }

        // 3. Fetch the consumption
        mockMvc.get("/metering-points/$gsrn/consumption") {
            param("from", "2026-10-23T00:00:00Z")
            param("to", "2026-10-24T00:00:00Z")
        }.andExpect {
            status { isOk() }
            jsonPath("$.quantityKwh") { value(2.0) }
        }
    }

    @Test
    fun `Test 2 - Duplicates in request`() {
        val gsrn = "707057500000000011"

        // 1. Create metering-point
        mockMvc.post("/metering-points") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"gsrn":"$gsrn","name":"Integrasjonstest"}"""
        }.andExpect { status { isCreated() } }

        // 2. Register duplicate values
        mockMvc.post("/metering-points/$gsrn/readings") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"readings":[
                    {"periodStart":"2026-10-23T10:00:00Z","quantityKwh":1.250},
                    {"periodStart":"2026-10-23T10:00:00Z","quantityKwh":0.750}
                ]}
            """
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `Test 3 - Duplicates in database`() {
        val gsrn = "707057500000000012"

        // 1. Create metering-point
        mockMvc.post("/metering-points") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"gsrn":"$gsrn","name":"Integrasjonstest"}"""
        }.andExpect { status { isCreated() } }

        // 2. Register value
        mockMvc.post("/metering-points/$gsrn/readings") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"readings":[
                    {"periodStart":"2026-10-23T10:00:00Z","quantityKwh":1.250}
                ]}
            """
        }.andExpect { status { isCreated() } }

        // 3. Register same value
        mockMvc.post("/metering-points/$gsrn/readings") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {"readings":[
                    {"periodStart":"2026-10-23T10:00:00Z","quantityKwh":1.250}
                ]}
            """
        }.andExpect { status { isConflict() } }
    }
}