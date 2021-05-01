package de.matthiaskainer.c4c.domain.commands

data class UpdateContract(
    override val provider : String,
    override val consumer : String,
    override val element  : String,
    override val fileLines: List<String>
): ContractInformation()