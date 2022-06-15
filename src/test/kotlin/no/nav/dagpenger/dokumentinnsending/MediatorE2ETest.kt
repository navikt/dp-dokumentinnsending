package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.PostgresTestHelper
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

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

            testRapid.sendTestMessage(ettersendingJson(behandlinskjedeId = eksternId, "jia66"))
            soknadRepository.hent(eksternId).also { soknad ->
                assertSoknadEquals(vistedOrginalSoknad, soknad,2)
                assertSoknadKomplett(soknad,false)
            }
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

private class MediatorTestVistor(soknad: Soknad?) : SoknadVisitor {
    lateinit var journalPostId: String
    lateinit var fodselsnummer: String
    lateinit var eksternSoknadId: String
    lateinit var registrertDato: ZonedDateTime
    var antallVedlegg: Int = 0

    init {
        require(soknad != null)
        soknad.accept(this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime
    ) {
        this.journalPostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.eksternSoknadId = eksternSoknadId
        this.registrertDato = registrertDato
    }

    override fun visitVedlegg(vedlegg: List<Vedlegg>) {
        this.antallVedlegg = vedlegg.size
    }
}