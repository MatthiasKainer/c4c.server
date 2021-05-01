package de.matthiaskainer.c4c.web.contract

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.matthiaskainer.c4c.web.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import java.nio.charset.StandardCharsets

object WhenRequestingContracts : Spek({
    fun Application.testApp() {
        startApp()
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
        install(Routing) {
            with(employeeRoutingForTests) { contract("/contracts") }
        }
    }
    test("returns list of contracts") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                Assertions.assertEquals(
                    listOf(testContract),
                    response.content?.toContracts()
                )
            }
        }
    }
    test("gets an existing single contract by id") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts/1")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                Assertions.assertEquals(
                    testContract,
                    response.content?.toContract()
                )
            }
        }
    }
    test("returns a 404 for a non-existing contract") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts/2")) {
                Assertions.assertEquals(
                    HttpStatusCode.NotFound,
                    response.status()
                )
            }
        }
    }
})