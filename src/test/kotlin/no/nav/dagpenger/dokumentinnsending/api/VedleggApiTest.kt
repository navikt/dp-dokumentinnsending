package no.nav.dagpenger.dokumentinnsending.api

import io.ktor.client.request.get
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.dokumentinnsending.api.TestApplication.autentisert
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VedleggApiTest {
    @Test
    fun `401 for requests uten gyldig token`() {
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = { vedleggApi(mockk()) }
        ) {
            assertEquals(401, client.get("v1/vedlegg/abcd").status.value)
        }
    }

    @Test
    fun `200 for request med gyldig tokent`() {
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = {
                vedleggApi(
                    mockk<SoknadRepository>().also {
                        every { it.hent("abcd") } returns null
                    }
                )
            }
        ) {
            client.get("v1/vedlegg/abcd") {
                autentisert()
            }.let { httpResponse ->
                assertEquals(404, httpResponse.status.value)
            }
        }
    }

    // @Test
    // fun `Skal vise vedlegg for en soknadsId`() {
    //     val mockk = mockk<SoknadRepository>(relaxed = true)
    //     TestApplication.withMockAuthServerAndTestApplication(
    //         moduleFunction = { vedleggApi(mockk) }
    //
    //     ) {
    //         client.get("v1/vedlegg/acbd123").let { httpResponse ->
    //             assertEquals(200, httpResponse.status.value)
    //         }
    //
    //     }
    // }
}
