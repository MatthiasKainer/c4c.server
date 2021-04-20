package de.matthiaskainer.c4c.repository.domain

import org.jetbrains.exposed.sql.Table

object ContractTable: Table() {
    val id = integer("id").autoIncrement()

    val provider = varchar("provider", 250)
    val consumer = varchar("consumer", 250)
    val element = varchar("element", 250)

    override val primaryKey = PrimaryKey(id)
}