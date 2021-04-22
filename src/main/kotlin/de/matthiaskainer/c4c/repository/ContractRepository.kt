package de.matthiaskainer.c4c.repository

import arrow.core.Either
import de.matthiaskainer.c4c.core.toEither
import de.matthiaskainer.c4c.domain.*
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.domain.errors.ContractProblem
import de.matthiaskainer.c4c.repository.domain.ContractTable
import de.matthiaskainer.c4c.repository.domain.FileLineTable
import de.matthiaskainer.c4c.repository.domain.TestResultTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun toContract(it: ResultRow) =
    Contract(
        it[ContractTable.id].toContractId(),
        it[ContractTable.provider],
        it[ContractTable.consumer],
        it[ContractTable.element],
        TestResultTable.select {
            TestResultTable.contractId eq it[ContractTable.id]
        }.map { result ->
            TestResult(
                result[TestResultTable.version],
                result[TestResultTable.executionDate],
                TestRunResult.valueOf(result[TestResultTable.result])
            )
        },
        FileLineTable.select {
            FileLineTable.contractId eq it[ContractTable.id]
        }.map { line ->
            line[FileLineTable.line]
        }
    )

class ContractRepository {
    suspend fun insert(c: CreateNewContract): Either<ContractProblem, ContractId> =
        toEither(ContractProblem.ContractCreationFailed) {
            transaction {
                val id = ContractTable.insert {
                    it[provider] = c.provider
                    it[consumer] = c.consumer
                    it[element] = c.element
                } get ContractTable.id
                FileLineTable.batchInsert(c.fileLines) { line ->
                    this[FileLineTable.line] = line
                    this[FileLineTable.contractId] = id
                }
                id
            }.toContractId()
        }


    fun findAll(): List<Contract> =
        transaction {
            ContractTable.selectAll().map {
                toContract(it)
            }
        }

    fun getById(id: ContractId?): Either<ContractProblem, Contract> =
        if (id == null) {
            Either.Left(ContractProblem.MissingIdForContract)
        } else {
            transaction {
                val result = ContractTable.select {
                    ContractTable.id eq id.value
                }
                Either.conditionally(
                    !result.empty(),
                    { ContractProblem.ContractNotFound },
                    { toContract(result.first()) })
            }
        }

    fun addTestResult(
        id: ContractId?,
        testResult: CreateNewTestResult
    ): Either<ContractProblem, TestResultId> =
        transaction {
            Either.conditionally(id != null && ContractTable.select {
                ContractTable.id eq id.value
            }.count() > 0, { ContractProblem.ContractNotFound }, { ->
                (TestResultTable.insert {
                    it[executionDate] = testResult.date
                    it[result] = testResult.result.toString()
                    it[version] = testResult.version
                    it[contractId] = id?.value
                } get TestResultTable.id).toTestResultId()
            })
        }
}