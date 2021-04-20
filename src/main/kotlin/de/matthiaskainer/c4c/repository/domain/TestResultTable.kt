package de.matthiaskainer.c4c.repository.domain

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object TestResultTable: Table() {
    val id = integer("id").autoIncrement()
    val version = varchar("version", 20)
    val executionDate = datetime("date")
    val result = varchar("result", 200)
    val contractId = (integer("contract_id") references ContractTable.id).nullable()
}