package de.matthiaskainer.c4c

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import de.matthiaskainer.c4c.core.Env
import de.matthiaskainer.c4c.core.config
import de.matthiaskainer.c4c.core.h2memConfig
import de.matthiaskainer.c4c.core.postgresConfig
import de.matthiaskainer.c4c.repository.ContractRepository
import de.matthiaskainer.c4c.repository.database.initDatasource
import de.matthiaskainer.c4c.web.ContractRouting
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val routing = ContractRouting(ContractRepository())

    val config = Env(mapOf(
        "dev" to config {
            database = h2memConfig
        },
        "prod" to config {
            database = postgresConfig
        }
    )).get()

    initDatasource(config.database)

    val server = embeddedServer(Netty, port = 8097) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
                registerModule(JavaTimeModule())
            }
        }
        install(Routing) {
            with(routing) {
                contract("/contracts")
            }
        }
    }
    server.start(wait = true)

}
