package de.matthiaskainer.c4c.domain.errors

sealed class ContractProblem {
    object ContractCreationFailed : ContractProblem()
    object ContractUpdateFailed : ContractProblem()
    object ContractNotFound : ContractProblem()
    object MissingIdForContract : ContractProblem()
    object MissingIdForVersion : ContractProblem()
    object InvalidRequestData: ContractProblem()
    object UnhandledIssue: ContractProblem()
    object InvalidContractId : ContractProblem()
    object VersionAlreadyExists : ContractProblem()
    object NoVersionsAvailable : ContractProblem()
}
