package de.matthiaskainer.c4c.domain.commands

data class CreateNewVersion(
    val version : String,
    val fileLines: List<String>
)