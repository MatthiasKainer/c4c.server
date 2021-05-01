package de.matthiaskainer.c4c.domain

import com.fasterxml.jackson.annotation.JsonValue

data class SemanticVersion(
    @get:JsonValue val version: String
    )

fun String?.toSemanticVersion() : SemanticVersion = this?.let {
    SemanticVersion(it) } ?: SemanticVersion("1.0.0")
