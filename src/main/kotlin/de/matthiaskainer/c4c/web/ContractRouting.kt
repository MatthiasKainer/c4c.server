package de.matthiaskainer.c4c.web

import arrow.core.flatMap
import de.matthiaskainer.c4c.core.toEither
import de.matthiaskainer.c4c.core.toResponse
import de.matthiaskainer.c4c.domain.ContractId
import de.matthiaskainer.c4c.domain.TestResult
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.domain.commands.CreateNewVersion
import de.matthiaskainer.c4c.domain.commands.UpdateContract
import de.matthiaskainer.c4c.domain.toContractId
import de.matthiaskainer.c4c.repository.ContractRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

data class CreateContractResult(val contractId: ContractId)

class ContractRouting(
    private val repository: ContractRepository
) {
    fun Routing.contract(path: String) {
        post(path) {
            toEither {
                call.receive<CreateNewContract>()
            }.flatMap {
                repository.insert(it)
            }.toResponse(call, HttpStatusCode.Created) {
                CreateContractResult(it)
            }
        }
        put(path) {
            toEither {
                call.receive<UpdateContract>()
            }.flatMap {
                repository.update(it)
            }.toResponse(call, HttpStatusCode.Created) {
                CreateContractResult(it)
            }
        }
        get(path) {
            call.respond(
                repository
                    .findAll()
            )
        }
        get("${path}/{id}") {
            call.parameters["id"].toContractId().flatMap {
                repository
                    .getById(it)
            }.toResponse(call, HttpStatusCode.OK)
        }
        post("${path}/{id}") {
            toEither {
                call.receive<CreateNewVersion>()
            }.flatMap { version ->
                call.parameters["id"].toContractId().flatMap {
                    repository.addVersion(it, version)
                }
            }.toResponse(call, HttpStatusCode.Created)
        }
        get("${path}/{id}/testResults") {
            call.parameters["id"].toContractId().flatMap {
                repository
                    .getById(it)
            }.toResponse(call, HttpStatusCode.OK) {
                it.versions.entries.fold(listOf<TestResult>()) { acc, entry ->
                    acc.plus(entry.value.testResults)
                }
            }
        }
        put("${path}/{id}/testResults") {
            toEither {
                call.receive<CreateNewTestResult>()
            }.flatMap {
                call.parameters["id"].toContractId()
                    .flatMap { contractId ->
                        repository
                            .addTestResult(
                                contractId,
                                it
                            )
                    }
            }.toResponse(call, HttpStatusCode.Accepted)
        }
        get("${path}/{id}/testResults/{version}") {
            call.parameters["id"].toContractId().flatMap {
                repository
                    .getByIdAndVersion(it, call.parameters["version"])
            }.toResponse(call, HttpStatusCode.OK)
        }
    }
}
