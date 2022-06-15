package no.nav.dagpenger.dokumentinnsending

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import java.time.LocalDateTime

internal class EttersendingMottakTest {
    private val testRapid = TestRapid()
/*
    @Test
    fun `håndterer innsending_mottatt hendelser av type ettersending`() {
        val mockMediator = mockk<Mediator>(relaxed = true)
        val forventetDato = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(søknadEttersendingJson(forventetDato.toLocalDateTime()))
        verify(exactly = 1) {
            mockMediator.handle(
                EttersendingMottattHendelse(
                    brukerBehandlingsId = "123hurra",
                    vedlegg = listOf<Vedlegg>()
                )
            )
        }
    }

    @Test
    fun `håndterer ikke ny søknad meldinger`() {
        val mockMediator = mockk<Mediator>(relaxed = true)
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(søknadInnsendingJson())
        verify(exactly = 0) {
            mockMediator.handle(any())
        }
    }
*/
}

//language=json
private fun søknadInnsendingJson() = """{
    
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123kkh",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "datoRegistrert": "${LocalDateTime.now()}",
  "søknadsData": {
    "brukerBehandlingId": "123hurra",
    "vedlegg": [],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()

//language=json
private fun søknadEttersendingJson(datoRegistrert: LocalDateTime) = """{
    
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123kkh",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "Ettersending",
  "datoRegistrert": "$datoRegistrert",
  "søknadsData": {
    "brukerBehandlingId": "123hurra",
    "vedlegg": [],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()
