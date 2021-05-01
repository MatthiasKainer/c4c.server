package de.matthiaskainer.c4c.web.contract

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

object WhenUpdatingAContract : Spek({
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

    test("updates the contract's test for the latest version only") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Post, "/contracts/${testContract.id.value}") {
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
            with(handleRequest(HttpMethod.Put, "/contracts") {
                addHeader(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
                setBody(
                    CreateNewContract(
                        testContract.provider,
                        testContract.consumer,
                        testContract.element,
                        listOf("new", "test")
                    ).toJSON()
                )
            }) {
                Assertions.assertEquals(HttpStatusCode.Created, response.status())
                Assertions.assertEquals(
                    "{\"contractId\":${testContract.id.value}}",
                    response.content
                )
            }
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                Assertions.assertEquals(HttpStatusCode.OK, response.status())
                Assertions.assertEquals(
                    ContentType.Application.Json.withCharset(StandardCharsets.UTF_8),
                    response.contentType()
                )
                val contracts = response.content?.toContracts()
                Assertions.assertEquals(
                    1,
                    contracts?.count()
                )
                Assertions.assertEquals(
                    2,
                    contracts?.first()?.versions?.count()
                )
                Assertions.assertEquals(
                    testContract.versions.values.first().fileLines,
                    contracts?.first()?.versions?.values?.first()?.fileLines
                )
                Assertions.assertEquals(
                    listOf("new", "test"),
                    contracts?.first()?.versions?.values?.last()?.fileLines
                )
            }
        }
    }
})