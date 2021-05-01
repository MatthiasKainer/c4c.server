package de.matthiaskainer.c4c.web.version

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewVersion
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

object WhenCreatingNewVersion: Spek({
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

    test("adds a new version") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts/1") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewVersion(
                        "1.0.1",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
            }
            with(handleRequest(HttpMethod.Get, "/contracts/1")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertNotNull(
                    response.content?.toContract()?.versions?.get("1.0.1")
                )
            }
        }
    }

    test("fails when adding the same version twice") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts/1") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewVersion(
                        "1.0.1",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
            }
            with(handleRequest(HttpMethod.Post, "/contracts/1") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewVersion(
                        "1.0.1",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Conflict, response.status())
            }
            with(handleRequest(HttpMethod.Get, "/contracts/1")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    2,
                    response.content?.toContract()?.versions?.entries?.size
                )
            }
        }
    }
})