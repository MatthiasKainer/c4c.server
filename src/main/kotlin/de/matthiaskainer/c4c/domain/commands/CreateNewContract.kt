package de.matthiaskainer.c4c.domain.commands

data class CreateNewContract(
    val provider : String,
    val consumer : String,
    val element  : String,
    val fileLines: List<String>
)