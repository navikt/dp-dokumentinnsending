package no.nav.dagpenger.dokumentinnsending

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import com.zaxxer.hikari.HikariDataSource

internal object Configuration {

    const val appName = "dp-dokumentinnsending"

    private val defaultProperties = ConfigurationMap(
        mapOf(
            "RAPID_APP_NAME" to appName,
            "KAFKA_CONSUMER_GROUP_ID" to "$appName-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_EXTRA_TOPIC" to "teamdagpenger.journalforing.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "AZURE_APP_WELL_KNOWN_URL" to "http://localhost:4443",
            "AZURE_APP_CLIENT_ID" to "azureClientId",
            "TOKEN_X_WELL_KNOWN_URL" to "http://localhost:4443",
            "TOKEN_X_CLIENT_ID" to "tokenxClientId"
        )
    )

    val properties: Configuration =
        ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }

    object AzureAd {
        const val name = "azureAd"
        val audience = properties[Key("AZURE_APP_CLIENT_ID", stringType)]
        val wellKnownUrl = properties[Key("AZURE_APP_WELL_KNOWN_URL", stringType)]
    }

    object TokenX {
        const val name = "tokenX"
        val audience = properties[Key("TOKEN_X_CLIENT_ID", stringType)]
        val wellKnownUrl = properties[Key("TOKEN_X_WELL_KNOWN_URL", stringType)]
    }

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", properties[Key("DB_HOST", stringType)])
            addDataSourceProperty("portNumber", properties[Key("DB_PORT", intType)])
            addDataSourceProperty("databaseName", properties[Key("DB_DATABASE", stringType)])
            addDataSourceProperty("user", properties[Key("DB_USERNAME", stringType)])
            addDataSourceProperty("password", properties[Key("DB_PASSWORD", stringType)])
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        }
    }
}
