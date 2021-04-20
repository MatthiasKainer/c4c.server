package de.matthiaskainer.c4c.core

import arrow.core.Either
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import de.matthiaskainer.c4c.domain.errors.ContractProblem
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

suspend fun <B> safeBlock(call: suspend () -> Either<ContractProblem, B>): Either<ContractProblem, B> =
    try {
        call()
    } catch (t: Throwable) {
        logger.warn { t }
        when (t) {
            is MissingKotlinParameterException -> Either.Left(ContractProblem.InvalidRequestData)
            is InvalidFormatException -> Either.Left(ContractProblem.InvalidRequestData)
            is ContentTransformationException -> Either.Left(ContractProblem.InvalidRequestData)
            else -> Either.Left(ContractProblem.UnhandledIssue)
        }
    }


private data class Result(val status: HttpStatusCode, val message: Any)

suspend fun <A,B> Either<A, B>.toResponse(
    call: ApplicationCall,
    successCode: HttpStatusCode,
    transform: ((result: B) -> Any)? = null
) =
    this.fold({
        when (it) {
            is ContractProblem.InvalidRequestData -> Result(HttpStatusCode.PreconditionFailed, "Invalid data provided")
            is ContractProblem.ContractNotFound -> Result(HttpStatusCode.NotFound, "Contract not found")
            is ContractProblem.ContractCreationFailed -> Result(HttpStatusCode.InternalServerError, "Contract creation has failed")
            else -> Result(HttpStatusCode.InternalServerError, "Unknown error")
        }
    },{
        Result(successCode, if (transform != null) transform(it) else it ?: "")
    })
        .let {
            call.respond(it.status, it.message)
        }
