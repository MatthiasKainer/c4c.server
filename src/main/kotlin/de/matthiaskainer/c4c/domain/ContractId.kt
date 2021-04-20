package de.matthiaskainer.c4c.domain

import com.fasterxml.jackson.annotation.JsonValue

data class ContractId (
    @get:JsonValue val value: Int
)
fun String.toContractId() = ContractId(this.toInt())
fun Int.toContractId() = ContractId(this)
