package de.matthiaskainer.c4c.repository.domain

import de.matthiaskainer.c4c.repository.domain.VersionTable.nullable
import de.matthiaskainer.c4c.repository.domain.VersionTable.references
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object TestResultTable: Table() {
    val id = integer("id").autoIncrement()
    val executionDate = datetime("date")
    val result = varchar("result", 200)
    val version = (integer("version_id").references(VersionTable.id))
    val contractId = (integer("contract_id") references ContractTable.id)
}