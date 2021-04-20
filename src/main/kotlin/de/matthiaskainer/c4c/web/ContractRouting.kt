package de.matthiaskainer.c4c.web

import de.matthiaskainer.c4c.core.toResponse
import de.matthiaskainer.c4c.core.safeBlock
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
            safeBlock {
                repository.insert(call.receive())
            }.toResponse(call, HttpStatusCode.Created)
        }
        get(path) {
            call.respond(
                repository
                    .findAll()
            )
        }
        get("${path}/{id}") {
            safeBlock {
                repository
                    .getById(call.parameters["id"]?.toContractId())
            }.toResponse(call, HttpStatusCode.OK)
        }
        get("${path}/{id}/testResults") {
            safeBlock {
                repository
                    .getById(call.parameters["id"]?.toContractId())
            }.toResponse(call, HttpStatusCode.OK) { it.testResults }
        }
        put("${path}/{id}/testResults") {
            safeBlock {
                repository
                    .addTestResult(
                        call.parameters["id"]?.toContractId(),
                        call.receive()
                    )
            }.toResponse(call, HttpStatusCode.Accepted)
        }
    }
}
