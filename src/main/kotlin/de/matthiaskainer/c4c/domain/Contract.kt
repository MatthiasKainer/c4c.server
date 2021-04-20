package de.matthiaskainer.c4c.domain

import java.time.LocalDateTime

enum class TestRunResult {
    Success,
    Failure
}

data class TestResult(
    val version: String,
    val date: LocalDateTime,
    val result: TestRunResult
)

data class Contract(
    val id : ContractId,
    val provider : String,
    val consumer : String,
    val element  : String,
    val testResults: List<TestResult>,
    val fileLines: List<String>
)

