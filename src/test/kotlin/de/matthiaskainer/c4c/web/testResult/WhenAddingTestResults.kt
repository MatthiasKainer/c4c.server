package de.matthiaskainer.c4c.web.testResult

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.matthiaskainer.c4c.domain.TestRunResult
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.web.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import java.time.LocalDateTime

object WhenAddingTestResults : Spek({
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
    test("adds test results to an existing contract and retrieves it successfully") {
        withTestApplication(Application::testApp) {
            val creationDate = LocalDateTime.parse("2021-04-27T14:59:38.850")
            with(handleRequest(HttpMethod.Put, "/contracts/1/testResults") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewTestResult(
                        creationDate,
                        TestRunResult.Success,
                        "1.0.0"
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Accepted, response.status())
            }
            with(handleRequest(HttpMethod.Get, "/contracts/1/testResults")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    2,
                    response.content?.toTestResults()?.size
                )
                Assertions.assertEquals(
                    TestRunResult.Success,
                    response.content?.toTestResults()?.first()?.result
                )
                Assertions.assertEquals(
                    creationDate,
                    response.content?.toTestResults()?.first()?.date
                )
            }
        }
    }
    test("fails to add a test result to an non-existing contract") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Put, "/contracts/2/testResults") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewTestResult(
                        LocalDateTime.now(),
                        TestRunResult.Success,
                        "1.0.0"
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
    test("creates a new version if it does not exist for a new test result") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Put, "/contracts/1/testResults") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewTestResult(
                        LocalDateTime.now(),
                        TestRunResult.Success,
                        "1.0.1"
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Accepted, response.status())
            }
        }
    }
})