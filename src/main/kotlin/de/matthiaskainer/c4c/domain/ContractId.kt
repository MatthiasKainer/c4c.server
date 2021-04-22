package de.matthiaskainer.c4c.domain

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonValue
import de.matthiaskainer.c4c.domain.errors.ContractProblem

data class ContractId(
    @get:JsonValue val value: Int
)

fun String?.toContractId() = this?.toIntOrNull()?.let {
    Either.Right(ContractId(it))
}  ?: Either.Left(ContractProblem.InvalidContractId)

fun Int.toContractId() = ContractId(this)
