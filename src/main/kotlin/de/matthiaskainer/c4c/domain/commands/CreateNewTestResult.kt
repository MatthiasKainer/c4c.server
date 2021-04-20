package de.matthiaskainer.c4c.domain.commands

import java.time.LocalDateTime

import de.matthiaskainer.c4c.domain.TestRunResult

data class CreateNewTestResult(
    val date: LocalDateTime,
    val result: TestRunResult,
    val version: String
)