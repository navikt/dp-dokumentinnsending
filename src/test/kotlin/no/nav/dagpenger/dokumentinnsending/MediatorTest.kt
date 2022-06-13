package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.PostgresTestHelper
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class MediatorE2ETest {
    private val testRapid = TestRapid()

    @Test
    fun `lagrer søknad med manglende vedlegg`() {
        PostgresTestHelper.withMigratedDb {
            val soknadRepository = PostgresSoknadRepository(PostgresTestHelper.dataSource)
            val mediator = Mediator(soknadRepository = soknadRepository)

            SoknadMottak(rapidsConnection = testRapid, mediator = mediator)

            testRapid.sendTestMessage(søknadJson("1"))

            val soknad = soknadRepository.hent("1")
            assertNotNull(soknad)
            assertFalse(soknad!!.erKomplett())
        }
    }

    @Test
    fun `lagrer søknad uten manglende vedlegg`() {
        PostgresTestHelper.withMigratedDb {
            val soknadRepository = PostgresSoknadRepository(PostgresTestHelper.dataSource)
            val mediator = Mediator(soknadRepository = soknadRepository)

            SoknadMottak(rapidsConnection = testRapid, mediator = mediator)

            testRapid.sendTestMessage(søknadJson("1"))

            val soknad = soknadRepository.hent("1")
            assertNotNull(soknad)
            assertTrue(soknad!!.erKomplett())
        }
    }
}

private fun søknadJson(brukerBehandlingsId: String, innsendingsValg: String = "SendesSenere") =

    """{
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "1236876",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "datoRegistrert": "${LocalDateTime.now()}",
  "søknadsData": {
    "brukerBehandlingId": "$brukerBehandlingsId",
    "vedlegg": [
    {
      "navn": "Etterlønn fra arbeidsgiver",
      "urls": {},
      "aarsak": null,
      "tittel": null,
      "filnavn": null,
      "faktumId": null,
      "mimetype": null,
      "soknadId": 11763,
      "storrelse": 506661,
      "vedleggId": 22225,
      "skjemaNummer": "K1",
      "opprettetDato": 1620374606000,
      "innsendingsvalg": "LastetOpp",
      "fillagerReferanse": "b1f4014f-51bf-4098-b56b-55902a4200b0",
      "skjemanummerTillegg": null,
      "opprinneligInnsendingsvalg": "LastetOpp"
    },
       {
      "navn": "Etterlønn fra arbeidsgiver",
      "urls": {},
      "aarsak": "sadf",
      "tittel": null,
      "filnavn": null,
      "faktumId": 850640,
      "mimetype": null,
      "soknadId": 11752,
      "storrelse": 0,
      "vedleggId": 22193,
      "skjemaNummer": "K1",
      "opprettetDato": 1620297587000,
      "innsendingsvalg": "$innsendingsValg",
      "fillagerReferanse": "47788575-f1fd-45c7-b0eb-a4f7242cf0b4",
      "skjemanummerTillegg": null,
      "opprinneligInnsendingsvalg": null
    }
    ],
    "skjemaNummer": "NAV12"
  } 
}
    """.trimIndent()
