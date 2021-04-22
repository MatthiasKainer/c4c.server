package de.matthiaskainer.c4c.web

import arrow.core.flatMap
import de.matthiaskainer.c4c.core.toEither
import de.matthiaskainer.c4c.core.toResponse
import de.matthiaskainer.c4c.domain.commands.CreateNewContract
import de.matthiaskainer.c4c.domain.commands.CreateNewTestResult
import de.matthiaskainer.c4c.domain.toContractId
import de.matthiaskainer.c4c.repository.ContractRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class ContractRouting(
    private val repository: ContractRepository
) {
    fun Routing.contract(path: String) {
        post(path) {
            toEither {
                call.receive<CreateNewContract>()
            }.flatMap {
                repository.insert(it)
            }.toResponse(call, HttpStatusCode.Created)
        }
        get(path) {
            call.respond(
                repository
                    .findAll()
            )
        }
        get("${path}/{id}") {
            call.parameters["id"]!!.toContractId().flatMap {
                repository
                    .getById(it)
            }.toResponse(call, HttpStatusCode.OK)
        }
        get("${path}/{id}/testResults") {
            call.parameters["id"]!!.toContractId().flatMap {
                repository
                    .getById(it)
            }.toResponse(call, HttpStatusCode.OK) { it.testResults }
        }
        put("${path}/{id}/testResults") {
            toEither {
                call.receive<CreateNewTestResult>()
            }.flatMap {
                call.parameters["id"]!!.toContractId()
                    .flatMap { contractId ->
                        repository
                            .addTestResult(
                                contractId,
                                it
                            )
                    }
            }.toResponse(call, HttpStatusCode.Accepted)
        }
    }
}
