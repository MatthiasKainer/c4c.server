package de.matthiaskainer.c4c.domain

import java.time.LocalDateTime

data class TestResult(
    val version: String,
    val date: LocalDateTime,
    val result: TestRunResult
)