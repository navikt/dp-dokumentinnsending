package no.nav.dagpenger.dokumentinnsending.db

import com.zaxxer.hikari.HikariDataSource
import kotliquery.using
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import javax.sql.DataSource

internal object PostgresTestHelper {

    val instance by lazy {
        PostgreSQLContainer<Nothing>("postgres:14").apply {
            this.waitingFor(HostPortWaitStrategy())
            start()
        }
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", instance.host)
            addDataSourceProperty("portNumber", instance.getMappedPort(POSTGRESQL_PORT))
            addDataSourceProperty("databaseName", instance.databaseName)
            addDataSourceProperty("user", instance.username)
            addDataSourceProperty("password", instance.password)
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }

    fun withMigratedDb(block: (ds: DataSource) -> Unit) {
        using(dataSource) { ds ->
            withCleanDb {
                runMigration(ds)
                block(ds)
            }
        }
    }

    fun withCleanDb(block: (ds: DataSource) -> Unit) {
        clean(dataSource).run {
            block(dataSource)
        }
    }
}
