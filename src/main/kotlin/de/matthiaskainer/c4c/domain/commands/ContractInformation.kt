package de.matthiaskainer.c4c.domain.commands

abstract class ContractInformation {
    abstract val provider : String
    abstract val consumer : String
    abstract val element  : String
    abstract val fileLines: List<String>
}