package no.nav.dagpenger.dokumentinnsending.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.dokumentinnsending.Configuration
import no.nav.security.mock.oauth2.MockOAuth2Server

internal object TestApplication {
    const val defaultDummyFodselsnummer = "123456789"

    private val mockOAuth2Server: MockOAuth2Server by lazy {
        MockOAuth2Server().also { server ->
            server.start()
        }
    }

    internal val tokenXToken: String by lazy {
        mockOAuth2Server.issueToken(
            issuerId = Configuration.TokenX.name,
            claims = mapOf(
                "sub" to defaultDummyFodselsnummer,
                "aud" to Configuration.TokenX.audience
            )
        ).serialize()
    }

    internal val azureAd: String by lazy {
        mockOAuth2Server.issueToken(
            issuerId = Configuration.AzureAd.name,
            claims = mapOf(
                "aud" to Configuration.AzureAd.audience
            )
        ).serialize()
    }

    internal fun withMockAuthServerAndTestApplication(
        moduleFunction: Application.() -> Unit,
        test: suspend ApplicationTestBuilder.() -> Unit
    ) {
        try {
            System.setProperty("TOKEN_X_WELL_KNOWN_URL", "${mockOAuth2Server.wellKnownUrl(Configuration.TokenX.name)}")
            System.setProperty(
                "AZURE_APP_WELL_KNOWN_URL",
                "${mockOAuth2Server.wellKnownUrl(Configuration.AzureAd.name)}"
            )
            testApplication {
                application(moduleFunction)
                test()
            }
        } finally {
        }
    }

    internal fun HttpRequestBuilder.autentisert(token: String = tokenXToken) {
        this.header(HttpHeaders.Authorization, "Bearer $token")
    }
}
