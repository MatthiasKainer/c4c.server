package de.matthiaskainer.c4c.domain

import com.fasterxml.jackson.annotation.JsonValue

data class TestResultId (
    @get:JsonValue val value: Int
)
fun String.toTestResultId() = TestResultId(this.toInt())
fun Int.toTestResultId() = TestResultId(this)
