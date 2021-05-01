package de.matthiaskainer.c4c.repository.serialisation

import de.matthiaskainer.c4c.domain.*
import de.matthiaskainer.c4c.repository.VersionResult
import de.matthiaskainer.c4c.repository.domain.ContractTable
import de.matthiaskainer.c4c.repository.domain.TestResultTable
import de.matthiaskainer.c4c.repository.domain.VersionTable
import org.jetbrains.exposed.sql.*

fun toContract(row: ResultRow): Contract =
    Contract(
        row[ContractTable.id].toContractId(),
        row[ContractTable.provider],
        row[ContractTable.consumer],
        row[ContractTable.element],
        toVersions(row[ContractTable.id].toContractId())
    )

fun toVersions(contractId: ContractId): Map<String, Version> =
    VersionTable.join(
        TestResultTable,
        JoinType.LEFT,
        additionalConstraint = { TestResultTable.version eq VersionTable.id })
        .select {
            VersionTable.contractId eq contractId.value
        }.orderBy(VersionTable.version to SortOrder.ASC)
        .fold(mapOf<String, Version>()) { acc, next ->
            if (acc.containsKey(next[VersionTable.version])) {
                acc.plus(
                    next[VersionTable.version] to combineVersions(
                        listOf(resultToVersion(next)).plus(acc[next[VersionTable.version]]!!)
                    )
                )
            } else
                acc.plus(
                    next[VersionTable.version] to resultToVersion(next)
                )
        }

private fun resultToVersion(next: ResultRow) = Version(
    next[VersionTable.id],
    if (next[TestResultTable.result] != null) listOf(
        TestResult(
            next[VersionTable.version],
            next[TestResultTable.executionDate],
            TestRunResult.valueOf(next[TestResultTable.result])
        )
    ) else emptyList(),
    next[VersionTable.lines].split("\n")
)

private fun combineVersions(versions: List<Version>) = Version(
    versions.first().id,
    versions.fold<Version, List<TestResult>>(listOf()) { acc, version ->
        acc.plus(
            version.testResults
        )
    }.sortedBy { it.date },
    versions.first().fileLines
)

fun toVersion(contractId: ContractId, version: String?): Version? =
    version?.let { v ->
        VersionTable.select {
            (VersionTable.contractId eq contractId.value).and(VersionTable.version eq v)
        }
    }?.orderBy(VersionTable.version to SortOrder.ASC)
        ?.map {
            VersionResult(
                it[VersionTable.id],
                it[VersionTable.version],
                it[VersionTable.lines].split("\n")
            )
        }
        ?.firstOrNull()
        ?.let { versionResult ->
            Version(
                versionResult.id,
                TestResultTable.select {
                    TestResultTable.version.eq(versionResult.id)
                }.map { result ->
                    TestResult(
                        versionResult.version,
                        result[TestResultTable.executionDate],
                        TestRunResult.valueOf(result[TestResultTable.result])
                    )
                },
                versionResult.lines
            )
        }
