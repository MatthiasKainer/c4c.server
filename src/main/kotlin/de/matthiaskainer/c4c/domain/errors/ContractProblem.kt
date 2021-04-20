package de.matthiaskainer.c4c.domain.errors

sealed class ContractProblem {
    object ContractCreationFailed : ContractProblem()
    object ContractNotFound : ContractProblem()
    object MissingIdForContract : ContractProblem()
    object InvalidRequestData: ContractProblem()
    object UnhandledIssue: ContractProblem()
}
