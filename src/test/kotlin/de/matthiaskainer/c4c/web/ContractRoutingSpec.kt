package de.matthiaskainer.c4c.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.matthiaskainer.c4c.core.Env
import de.matthiaskainer.c4c.core.config
import de.matthiaskainer.c4c.domain.Contract
import de.matthiaskainer.c4c.domain.ContractId
import de.matthiaskainer.c4c.domain.TestResult
import de.matthiaskainer.c4c.domain.TestRunResult
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.repository.ContractRepository
import de.matthiaskainer.c4c.repository.database.initDatasource
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDateTime

val mapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
fun String.toContracts() =
    mapper.readValue<List<Contract>>(this)
fun String.toContract() =
    mapper.readValue<Contract>(this)
fun CreateNewContract.toJSON(): String =
    mapper.writeValueAsString(this)
fun CreateNewTestResult.toJSON(): String =
    mapper.writeValueAsString(this)

val testContract = Contract(
    ContractId(1),
    "provider",
    "consumer",
    "element",
    listOf(TestResult("1.0.0.0", date = LocalDateTime.of(2021, 6, 12, 0, 0), result = TestRunResult.Success)),
    listOf("contract")
)

object ContractRoutingSpec : Spek({

    val repository = ContractRepository()
    val employeeRoutingForTests = ContractRouting(repository)

    val config = Env(mapOf(
        "test" to config {
            database {
                this withUrl "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" withDriver "org.h2.Driver" withUser "root"
            }
        }
    )).get()

    fun Application.testApp() {
        initDatasource(config.database, clean=true)
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
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    CreateNewContract("provider", "consumer", "new element", emptyList()).toJSON()
                )
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
            }
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Application.Json.withCharset(UTF_8), response.contentType())
                assertEquals(
                    2,
                    response.content?.toContracts()?.count()
                )
                assertEquals(
                    "new element",
                    response.content?.toContracts()?.last()?.element
                )
            }
        }
    }
    test("returns list of contracts") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Application.Json.withCharset(UTF_8), response.contentType())
                assertEquals(
                    listOf(testContract),
                    response.content?.toContracts()
                )
            }
        }
    }
    test("gets an existing single contract by id") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts/1")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(ContentType.Application.Json.withCharset(UTF_8), response.contentType())
                assertEquals(
                    testContract,
                    response.content?.toContract()
                )
            }
        }
    }
    test("returns a 404 for a non-existing contract") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Get, "/contracts/2")) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
    test("adds a test result to an existing contract and retrieves it successfully") {
        withTestApplication(Application::testApp) {
            with(handleRequest(HttpMethod.Put, "/contracts/1/testResults") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    CreateNewTestResult(LocalDateTime.now(), TestRunResult.Success, "1.0.0.0").toJSON()
                )
            }) {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
            with(handleRequest(HttpMethod.Get, "/contracts/1/testResults")) {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

})
