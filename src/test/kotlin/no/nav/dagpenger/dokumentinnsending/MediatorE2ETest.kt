package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.PostgresTestHelper
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertContentEquals

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
            // hva vil vi skal skje om det kommer en NySøknad melding som er duplikat?Skjer det?
        }
    }

    @Test
    fun `lagrer søknad uten manglende vedlegg og lagrer ettersendinger`() {
        val eksternId = "haksj1"
        PostgresTestHelper.withMigratedDb {
            val soknadRepository = PostgresSoknadRepository(PostgresTestHelper.dataSource)
            val mediator = Mediator(soknadRepository = soknadRepository)

            SoknadMottak(rapidsConnection = testRapid, mediator = mediator)
            EttersendingMottak(rapidsConnection = testRapid, mediator = mediator)

            testRapid.sendTestMessage(søknadJson(brukerBehandlingsId = eksternId, innsendingsValg = "LastetOpp"))
            val originalSoknad = soknadRepository.hent(eksternId)
            val vistedOrginalSoknad = MediatorTestVistor(originalSoknad)
            require(originalSoknad != null)
            assertSoknadKomplett(originalSoknad, true)
            assertEquals(1, originalSoknad.aktivitetslogg.aktivitetsteller())
            assertAktivitetslogg(
                logg = vistedOrginalSoknad.aktivitetslogg,
                meldinger = listOf("Søknad motatt"),
                antallInfo = 1
            )

            testRapid.sendTestMessage(
                ettersendingJson2(
                    behandlinskjedeId = eksternId,
                    brukerBehandlingsId = "lggfjhe",
                    innsendingsValg1 = "LastetOpp",
                    innsendingsValg2 = "SendesSenere"
                )
            )
            soknadRepository.hent(eksternId).also { soknad ->
                requireNotNull(soknad)
                assertSoknadEquals(vistedOrginalSoknad, soknad, 2)
                assertSoknadKomplett(soknad, false)
                assertEquals(2, soknad.aktivitetslogg.aktivitetsteller())
                assertAktivitetslogg(soknad.aktivitetslogg, listOf("Søknad motatt", "Ettersending motatt"), 2)
            }

            testRapid.sendTestMessage(
                ettersendingJson2(
                    behandlinskjedeId = eksternId,
                    brukerBehandlingsId = "lggfjhe",
                    innsendingsValg1 = "LastetOpp",
                    innsendingsValg2 = "LastetOpp"
                )
            )
            soknadRepository.hent(eksternId).also { soknad ->
                requireNotNull(soknad)
                assertSoknadEquals(vistedOrginalSoknad, soknad, 2)
                assertSoknadKomplett(soknad, true)
                assertEquals(3, soknad.aktivitetslogg.aktivitetsteller())
                assertAktivitetslogg(
                    soknad.aktivitetslogg,
                    listOf("Søknad motatt", "Ettersending motatt", "Ettersending motatt"),
                    3
                )
            }
        }
    }

    private fun assertAktivitetslogg(logg: Aktivitetslogg, meldinger: List<String>, antallInfo: Int) {
        AktivitetsloggTestVisitor(logg).also { aktivitetslogg ->
            assertEquals(0, aktivitetslogg.antWarnings)
            assertEquals(0, aktivitetslogg.antErrors)
            assertEquals(0, aktivitetslogg.antSevere)
            assertEquals(antallInfo, aktivitetslogg.antInfo)
            assertContentEquals(meldinger, aktivitetslogg.meldinger)
        }
    }

    private fun assertSoknadKomplett(soknad: Soknad?, komplett: Boolean) {
        require(soknad != null)
        assertEquals(komplett, soknad.erKomplett())
    }

    private fun assertSoknadEquals(expectedVisitor: MediatorTestVistor, actual: Soknad?, expectedAntallVedlegg: Int) {
        MediatorTestVistor(actual).also { actualVerdier ->
            assertEquals(expectedVisitor.antallVedlegg, expectedAntallVedlegg)
            assertEquals(expectedVisitor.journalPostId, actualVerdier.journalPostId)
            assertEquals(expectedVisitor.fodselsnummer, actualVerdier.fodselsnummer)
            assertEquals(expectedVisitor.eksternSoknadId, actualVerdier.eksternSoknadId)
            assertEquals(expectedVisitor.registrertDato, actualVerdier.registrertDato)
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

private fun ettersendingJson(
    behandlinskjedeId: String,
    brukerBehandlingsId: String,
    innsendingsValg1: String = "LastetOpp",
    innsendingsValg2: String = "SendesSenere"
) =
    //language=json
    """{
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123kkh",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "Ettersending",
  "datoRegistrert": "${LocalDateTime.now()}",
  "søknadsData": {
    "brukerBehandlingId": "$brukerBehandlingsId",
    "behandlingskjedeId": "$behandlinskjedeId",
    "vedlegg": [
        {
          "navn": "SJOKKERENDE ELEKTRIKER",
          "urls": {},
          "aarsak": null,
          "tittel": null,
          "filnavn": null,
          "faktumId": null,
          "mimetype": null,
          "soknadId": 5220533,
          "storrelse": 0,
          "vedleggId": 30666849,
          "skjemaNummer": "T8",
          "opprettetDato": 1655205510121,
          "innsendingsvalg": "$innsendingsValg1",
          "fillagerReferanse": "0f3ca88b-a70e-4440-926c-a12fc8d7666f",
          "skjemanummerTillegg": "sagtoppavarbeidsgiver",
          "opprinneligInnsendingsvalg": "SendesSenere"
        },
        {
          "navn": "SJOKKERENDE ELEKTRIKER",
          "urls": {},
          "aarsak": null,
          "tittel": null,
          "filnavn": null,
          "faktumId": null,
          "mimetype": null,
          "soknadId": 5220533,
          "storrelse": 125816,
          "vedleggId": 30666848,
          "skjemaNummer": "O2",
          "opprettetDato": 1655205510115,
          "innsendingsvalg": "LastetOpp",
          "fillagerReferanse": "ae3a01d6-18ef-443e-add1-701b8a7e631e",
          "skjemanummerTillegg": "sagtoppavarbeidsgiver",
          "opprinneligInnsendingsvalg": "$innsendingsValg2"
        }
      ]
    "skjemaNummer": "NAV12"
  }
}
    """.trimIndent()

private fun ettersendingJson2(
    datoRegistrert: LocalDateTime = LocalDateTime.now(),
    behandlinskjedeId: String,
    brukerBehandlingsId: String,
    innsendingsValg1: String = "LastetOpp",
    innsendingsValg2: String = "SendesSenere"

) = """{
    
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "${(0..10000).random()}",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "Ettersending",
  "datoRegistrert": "$datoRegistrert",
  "søknadsData": {
    "behandlingskjedeId": "$behandlinskjedeId",
    "brukerBehandlingId": "$brukerBehandlingsId",
    "vedlegg": [
    {
      "navn": "SJOKKERENDE ELEKTRIKER",
      "urls": {},
      "aarsak": "afsd",
      "tittel": null,
      "filnavn": null,
      "faktumId": 380263325,
      "mimetype": null,
      "soknadId": 5220529,
      "storrelse": 0,
      "vedleggId": 30666838,
      "skjemaNummer": "O2",
      "opprettetDato": 1654780179734,
      "innsendingsvalg": "$innsendingsValg1",
      "fillagerReferanse": "14f7abee-4eb9-43e8-9031-6ee802725479",
      "skjemanummerTillegg": "sagtoppavarbeidsgiver",
      "opprinneligInnsendingsvalg": null
    },
    {
      "navn": "SJOKKERENDE ELEKTRIKER",
      "urls": {},
      "aarsak": "saf",
      "tittel": null,
      "filnavn": null,
      "faktumId": 380263325,
      "mimetype": null,
      "soknadId": 5220529,
      "storrelse": 0,
      "vedleggId": 30666839,
      "skjemaNummer": "T8",
      "opprettetDato": 1654780179744,
      "innsendingsvalg": "$innsendingsValg2",
      "fillagerReferanse": "8e557fc9-4a6d-4b52-8159-bf2c94450018",
      "skjemanummerTillegg": "sagtoppavarbeidsgiver",
      "opprinneligInnsendingsvalg": null
    }
  ],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()
