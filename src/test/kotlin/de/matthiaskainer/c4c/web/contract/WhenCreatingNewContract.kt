package de.matthiaskainer.c4c.web.contract

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.web.employeeRoutingForTests
import de.matthiaskainer.c4c.web.startApp
import de.matthiaskainer.c4c.web.toContracts
import de.matthiaskainer.c4c.web.toJSON
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.spekframework.spek2.Spek
import java.nio.charset.StandardCharsets

object WhenCreatingNewContract : Spek({
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

    test("adds a new contract") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        "provider",
                        "consumer",
                        "new element",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
                Assertions.assertEquals(
                    "{\"contractId\":2}",
                    response.content
                )
            }
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                Assertions.assertEquals(
                    2,
                    response.content?.toContracts()?.count()
                )
                Assertions.assertEquals(
                    "new element",
                    response.content?.toContracts()?.last()?.element
                )
            }
        }
    }

    test("does not add the same contract twice") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        "provider",
                        "consumer",
                        "new element",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
            }
            with(handleRequest(HttpMethod.Post, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        "provider",
                        "consumer",
                        "new element",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
                Assertions.assertEquals(
                    "{\"contractId\":2}",
                    response.content
                )
            }
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                Assertions.assertEquals(
                    2,
                    response.content?.toContracts()?.count()
                )
            }
        }
    }

    test("updates the contract's test for the latest version") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        "provider",
                        "consumer",
                        "new element",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
            }
            with(handleRequest(HttpMethod.Post, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        "provider",
                        "consumer",
                        "new element",
                        emptyList()
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
                Assertions.assertEquals(
                    "{\"contractId\":2}",
                    response.content
                )
            }
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                Assertions.assertEquals(
                    2,
                    response.content?.toContracts()?.count()
                )
            }
        }
    }
})