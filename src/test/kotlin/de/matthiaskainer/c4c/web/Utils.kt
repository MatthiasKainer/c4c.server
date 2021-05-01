package de.matthiaskainer.c4c.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import de.matthiaskainer.c4c.core.Env
import de.matthiaskainer.c4c.core.config
import de.matthiaskainer.c4c.domain.*
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.domain.commands.CreateNewVersion
import de.matthiaskainer.c4c.repository.ContractRepository
import de.matthiaskainer.c4c.repository.database.initDatasource
import java.time.LocalDateTime

val mapper: ObjectMapper =
    jacksonObjectMapper().registerModule(JavaTimeModule())

fun String.toContracts() =
    mapper.readValue<List<Contract>>(this)

fun String.toContract() =
    mapper.readValue<Contract>(this)

fun String.toTestResults() =
    mapper.readValue<List<TestResult>>(this)
fun String.toVersion() =
    mapper.readValue<Version>(this)

fun CreateNewContract.toJSON(): String =
    mapper.writeValueAsString(this)

fun CreateNewTestResult.toJSON(): String =
    mapper.writeValueAsString(this)

fun CreateNewVersion.toJSON(): String =
    mapper.writeValueAsString(this)

val testContract = Contract(
    ContractId(1),
    "provider",
    "consumer",
    "element-example",
    mapOf(
        "1.0.0" to Version(
            1,
            listOf(
                TestResult(
                    "1.0.0",
                    date = LocalDateTime.of(2021, 6, 12, 0, 0),
                    result = TestRunResult.Success
                )
            ),
            listOf("const some='lines';", "")
        )
    )
)

val repository = ContractRepository()
val employeeRoutingForTests = ContractRouting(repository)

val config = Env(mapOf(
    "test" to config {
        database {
            this withUrl "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" withDriver "org.h2.Driver" withUser "root"
        }
    }
)).get()

fun startApp() {
    initDatasource(config.database, clean = true)
}
