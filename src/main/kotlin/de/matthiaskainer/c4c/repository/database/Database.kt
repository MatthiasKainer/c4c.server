package de.matthiaskainer.c4c.repository.database

import de.matthiaskainer.c4c.core.DatabaseConfiguration
import de.matthiaskainer.c4c.domain.TestRunResult
import de.matthiaskainer.c4c.repository.domain.ContractTable
import de.matthiaskainer.c4c.repository.domain.VersionTable
import de.matthiaskainer.c4c.repository.domain.TestResultTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

fun connect(databaseConfiguration: DatabaseConfiguration) {
    org.jetbrains.exposed.sql.Database.connect(
        databaseConfiguration.url,
        driver = databaseConfiguration.driver,
        user = databaseConfiguration.user,
        password = databaseConfiguration.password
    )
}

fun initDatasource(databaseConfiguration: DatabaseConfiguration, clean: Boolean = false) {
    connect(databaseConfiguration)

    transaction {
        if (clean) SchemaUtils.drop(ContractTable, VersionTable, TestResultTable)
        SchemaUtils.create(ContractTable, VersionTable, TestResultTable)

        val demoContract = ContractTable.insert {
            it[consumer] = "consumer"
            it[provider] = "provider"
            it[element] = "element-example"
        } get ContractTable.id

        val versionId = VersionTable.insert {
            it[version] = "1.0.0"
            it[lines] = "const some='lines';\n"
            it[contractId] = demoContract
        } get VersionTable.id

        TestResultTable.insert {
            it[executionDate] = LocalDateTime.of(2021, 6, 12, 0, 0)
            it[result] = TestRunResult.Success.toString()
            it[version] = versionId
            it[contractId] = demoContract
        }
    }
}