package no.nav.dagpenger.dokumentinnsending

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.dokumentinnsending.modell.EttersendingMottattHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class EttersendingMottakTest {

    private val testRapid = TestRapid()
    val mockMediator = mockk<Mediator>(relaxed = true)
    val hendelseSlot = slot<EttersendingMottattHendelse>()

    init {
        EttersendingMottak(testRapid, mockMediator)
    }

    @Test
    fun `håndterer innsending_mottatt hendelser av type Ettersending`() {
        hendelseSlot.clear()
        val forventetDato = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        testRapid.sendTestMessage(ettersendingJson(forventetDato.toLocalDateTime()))
        verify(exactly = 1) {
            mockMediator.handle(
                capture(hendelseSlot)
            )
        }
        val actual = hendelseSlot.captured
        assertEquals("123kkh", actual.journalpostId())
        assertEquals("123hurra8", actual.eksternSoknadId())
        assertEquals(2, actual.vedlegg().size)
    }

    @Test
    fun `håndterer ikke meldinger av type nySøknad`() {
        hendelseSlot.clear()
        testRapid.sendTestMessage(nySøknadJson)
        verify(exactly = 0) {
            mockMediator.handle(
                capture(hendelseSlot)
            )
        }
    }
}

//language=json
private fun ettersendingJson(datoRegistrert: LocalDateTime) = """{
    
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123kkh",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "Ettersending",
  "datoRegistrert": "$datoRegistrert",
  "søknadsData": {
    "behandlingskjedeId": "123hurra8",
    "brukerBehandlingId": "123hurra",
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
      "innsendingsvalg": "SendesSenere",
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
      "innsendingsvalg": "SendesSenere",
      "fillagerReferanse": "8e557fc9-4a6d-4b52-8159-bf2c94450018",
      "skjemanummerTillegg": "sagtoppavarbeidsgiver",
      "opprinneligInnsendingsvalg": null
    }
  ],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()

private val nySøknadJson = """{
//language=json
  "@id": "123",
  "@opprettet": "2021-01-01T01:01:01.000001",
  "journalpostId": "12455",
  "datoRegistrert": "2021-01-01T01:01:01.000001",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "fødselsnummer": "11111111111",
  "aktørId": "1234455",
  "søknadsData": "søknadsData": {
    "brukerBehandlingId": "123hurra",
    "vedlegg": [],
    "skjemaNummer": "NAV12"
  },
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}
""".trimIndent()

