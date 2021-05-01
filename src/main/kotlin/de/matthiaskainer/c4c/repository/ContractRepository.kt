package de.matthiaskainer.c4c.repository

import arrow.core.Either
import de.matthiaskainer.c4c.core.toEither
import de.matthiaskainer.c4c.domain.*
import de.matthiaskainer.c4c.domain.commands.*
import de.matthiaskainer.c4c.domain.errors.ContractProblem
import de.matthiaskainer.c4c.repository.domain.ContractTable
import de.matthiaskainer.c4c.repository.domain.TestResultTable
import de.matthiaskainer.c4c.repository.domain.VersionTable
import de.matthiaskainer.c4c.repository.serialisation.toContract
import de.matthiaskainer.c4c.repository.serialisation.toVersion
import de.matthiaskainer.c4c.repository.serialisation.toVersions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class VersionResult(
    val id: Int,
    val version: String,
    val lines: List<String>
)

class ContractRepository {
    suspend fun insert(c: CreateNewContract): Either<ContractProblem, ContractId> =
        toEither(ContractProblem.ContractCreationFailed) {
            transaction {
                val existing = ContractTable.select {
                    ContractTable.provider.eq(c.provider) and
                            (ContractTable.consumer eq c.consumer and
                                    (ContractTable.element eq c.element))
                }
                if (!existing.empty()) existing.first()[ContractTable.id]
                else {
                    insertContract(c)
                }
            }.toContractId()
        }


    suspend fun update(update: UpdateContract) =
        toEither(ContractProblem.ContractUpdateFailed) {
            transaction {
                val existing = toContract(ContractTable.select {
                    ContractTable.provider.eq(update.provider) and
                            (ContractTable.consumer eq update.consumer and
                                    (ContractTable.element eq update.element))
                }.first())
                updateVersion(
                    existing.id,
                    existing.versions.keys.last(),
                    update.fileLines
                )
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

    fun getByIdAndVersion(
        id: ContractId?,
        version: String?
    ): Either<ContractProblem, Version> =
        if (id == null) {
            Either.Left(ContractProblem.MissingIdForContract)
        } else {
            transaction {
                toVersion(id, version)?.let {
                    Either.Right(it)
                } ?: Either.Left(ContractProblem.ContractNotFound)
            }
        }

    fun addTestResult(
        id: ContractId?,
        testResult: CreateNewTestResult
    ): Either<ContractProblem, TestResultId> =
        transaction {
            id?.let {
                val versions = toVersions(id)
                if (versions.isEmpty()) Either.Left(ContractProblem.NoVersionsAvailable)
                else {
                    val versionId: Int =
                        versions[testResult.version]?.id ?: insertVersion(
                            id,
                            CreateNewVersion(
                                testResult.version,
                                versions.values.first().fileLines
                            )
                        ).value

                    Either.Right(
                        (TestResultTable.insert {
                            it[executionDate] = testResult.date
                            it[result] = testResult.result.toString()
                            it[version] = versionId
                            it[contractId] = id.value
                        } get TestResultTable.id).toTestResultId()
                    )
                }
            } ?: Either.Left(ContractProblem.ContractNotFound)
        }

    fun addVersion(contractId: ContractId, version: CreateNewVersion)
            : Either<ContractProblem, VersionId> =
        transaction {
            val existingVersion = VersionTable.select {
                VersionTable.contractId.eq(contractId.value) and
                        VersionTable.version.eq(version.version)
            }.firstOrNull()
            Either.conditionally(existingVersion == null,
                { ContractProblem.VersionAlreadyExists }, { ->
                    insertVersion(contractId, version)
                })
        }

    private fun insertVersion(
        contractId: ContractId,
        version: CreateNewVersion
    ): VersionId = (VersionTable.insert {
        it[VersionTable.contractId] = contractId.value
        it[VersionTable.version] = version.version
        it[lines] =
            version.fileLines.joinToString("\n")
    } get VersionTable.id).toVersionId()

    private fun updateVersion(
        contractId: ContractId,
        version: String,
        fileLines: List<String>
    ) = VersionTable.update({
        VersionTable.contractId eq contractId.value and (VersionTable.version eq version)
    }) {
        it[lines] = fileLines.joinToString("\n")
    }

    private fun <T> insertContract(c: T): Int where T : ContractInformation {
        val id = ContractTable.insert {
            it[provider] = c.provider
            it[consumer] = c.consumer
            it[element] = c.element
        } get ContractTable.id
        VersionTable.insert {
            it[version] = "0.0.0"
            it[lines] = c.fileLines.joinToString("\n")
            it[contractId] = id
        }
        return id
    }

}