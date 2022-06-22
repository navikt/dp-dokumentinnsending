package no.nav.dagpenger.dokumentinnsending.api

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.dokumentinnsending.api.TestApplication.autentisert
import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.PostgresTestHelper
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagSoknad
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
    fun `skal kunne liste søknader som ikke er eldre enn 12 uker for en person`() {
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
                    val tadda = httpResponse.bodyAsText()
                    assertEquals(200, httpResponse.status.value)
                    JSONAssert.assertEquals(expectedSoknadResponsJson(now), tadda, false)
                }
            }
        }
    }

    private fun expectedSoknadResponsJson(registrertDato: ZonedDateTime): String {
        //language=Json
        return """[
  {
    "registrertDato": "${registrertDato.toJsonApiFormat()}",
    "innsendingsId": 1,
    "vedlegg": [
      {
        "navn": "vedlegg1",
        "status": "INNSENDT"
      },
      {
        "navn": "vedlegg2",
        "status": "INNSENDT"
      }
    ]
  },
  {
    "registrertDato": "${registrertDato.toJsonApiFormat()}",
    "innsendingsId": 2,
    "vedlegg": []
  }
]
        """.trimIndent()
    }

    private fun DataSource.leggInnSoknad(fnr: String, registrertDato: ZonedDateTime) {
        PostgresSoknadRepository(this).let { repo ->
            lagSoknad(
                eksternSoknadId = "tadda77",
                journalpostId = "1",
                fnr = fnr,
                registrertDato = registrertDato,
                vedlegg = listOf(
                    lagInnsendtVedlegg(datoRegistrert = registrertDato, navn = "vedlegg1"),
                    lagInnsendtVedlegg(datoRegistrert = registrertDato, navn = "vedlegg2")
                )
            ).also { repo.lagre(it) }
            lagSoknad(
                eksternSoknadId = "tadda88",
                journalpostId = "2",
                fnr = fnr,
                registrertDato = registrertDato,
                vedlegg = emptyList()
            ).also { repo.lagre(it) }
        }
    }
}

private fun ZonedDateTime.toJsonApiFormat() = this.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
