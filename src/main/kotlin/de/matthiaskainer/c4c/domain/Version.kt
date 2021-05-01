package de.matthiaskainer.c4c.domain

data class Version(
    val id: Int,
    val testResults: List<TestResult>,
    val fileLines: List<String>
)