package no.nav.dagpenger.dokumentinnsending.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.dokumentinnsending.SoknadMedVedlegg
import no.nav.dagpenger.dokumentinnsending.api.TestApplication.autentisert
import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.PostgresTestHelper
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.lagSoknader
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZonedDateTime
import javax.sql.DataSource
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

    @Test
    fun `skal kunne list søknader som ikke er eldre enn 12 uker for en person`() {
        val now = ZonedDateTime.now()
        PostgresTestHelper.withMigratedDb { ds ->
            ds.leggInnSoknad(TestApplication.defaultDummyFodselsnummer, now)
            TestApplication.withMockAuthServerAndTestApplication(
                moduleFunction = {
                    vedleggApi(soknadRepository = PostgresSoknadRepository(ds))
                }
            ) {
                client.get("v1/soknader") {
                    autentisert()
                }.let { httpResponse ->
                    assertEquals(200, httpResponse.status.value)
                    JSONAssert.assertEquals(expectedSoknadResponsJson(now), httpResponse.bodyAsText(), true)
                }
            }
        }
    }

    private fun expectedSoknadResponsJson(registrertDato: ZonedDateTime): String {
        //language=Json
        return """[
      {
        "registrertDato": "$registrertDato"
        "innsendingsId": 1,
        "vedlegg": [
          {
            "navn": "1",
            "status": "INNSENDT"
          },
          {
            "navn": "2",
            "status": "INNSENDT"
          },
          {
            "navn": "3",
            "status": "INNSENDT"
          }
        ]
      },
      {
        "registrertDato": "$registrertDato"
        "innsendingsId": 2,
        "vedlegg": [
          {
            "navn": "1",
            "status": "INNSENDT"
          },
          {
            "navn": "2",
            "status": "INNSENDT"
          },
          {
            "navn": "3",
            "status": "INNSENDT"
          }
        ]
      }
    ]
        """.trimIndent()
    }

    private fun DataSource.leggInnSoknad(fnr: String, registrertDato: ZonedDateTime) {
        PostgresSoknadRepository(this).let { repo ->
            lagSoknader(fnr, SoknadMedVedlegg(1, 3), SoknadMedVedlegg(2, 3)).forEach { repo.lagre(it) }
        }
    }
}
