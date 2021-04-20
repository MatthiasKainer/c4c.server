package de.matthiaskainer.c4c.repository.domain

import org.jetbrains.exposed.sql.Table

object FileLineTable: Table() {
    val id = integer("id").autoIncrement()
    val line = text("line")
    val contractId = (integer("contract_id") references ContractTable.id).nullable()
}

