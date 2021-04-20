package de.matthiaskainer.c4c.core

class DatabaseConfiguration {
    var url: String = System.getenv("database_url") ?: ""
    var driver: String = System.getenv("database_driver") ?: ""
    var user: String = System.getenv("database_user") ?: ""
    var password: String = System.getenv("database_password") ?: ""

    infix fun withUrl(url: String) = apply {
        this.url = url
    }

    infix fun withDriver(driver: String) = apply {
        this.driver = driver
    }

    infix fun withUser(user: String) = apply {
        this.user = user
    }

    infix fun withPassword(password: String) = apply {
        this.password = password
    }
}

fun database(init: DatabaseConfiguration.() -> Unit) =
    DatabaseConfiguration().apply(init)


class Configuration {
    var database: DatabaseConfiguration = DatabaseConfiguration()

    fun database(init: DatabaseConfiguration.() -> Unit) {
        this.database = DatabaseConfiguration().apply(init)
    }
}

fun config(initializer: Configuration.() -> Unit): Configuration {
    return Configuration().apply(initializer)
}

class Env(private val envs: Map<String, Configuration>) {
    fun get() = envs[System.getenv("ENV")] ?: envs.values.first()
}

val h2memConfig = database {
    this withUrl "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" withDriver "org.h2.Driver" withUser "root"
}

val postgresConfig = database {
    this withDriver "org.postgresql.Driver"
}