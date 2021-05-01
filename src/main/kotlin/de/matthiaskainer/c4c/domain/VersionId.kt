package de.matthiaskainer.c4c.domain

import com.fasterxml.jackson.annotation.JsonValue

data class VersionId (
    @get:JsonValue val value: Int
)
fun String.toVersionId() = VersionId(this.toInt())
fun Int.toVersionId() = VersionId(this)
