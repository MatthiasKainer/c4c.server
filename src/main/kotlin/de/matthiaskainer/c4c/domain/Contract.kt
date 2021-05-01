package de.matthiaskainer.c4c.domain

data class Contract(
    val id : ContractId,
    val provider : String,
    val consumer : String,
    val element  : String,
    val versions: Map<String, Version>
)

