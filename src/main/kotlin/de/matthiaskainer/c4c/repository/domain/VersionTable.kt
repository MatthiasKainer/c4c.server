package de.matthiaskainer.c4c.repository.domain

import org.jetbrains.exposed.sql.Table

object VersionTable: Table() {
    val id = integer("id").autoIncrement()
    val version = varchar("version", 255)
    val lines = text("lines")
    val contractId = (integer("contract_id") references ContractTable.id).nullable()

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(version, contractId, name = "pk_versionkey")
}

