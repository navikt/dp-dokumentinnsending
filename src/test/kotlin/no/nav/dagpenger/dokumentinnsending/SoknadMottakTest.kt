package no.nav.dagpenger.dokumentinnsending

import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SoknadMottakTest {
    private val testRapid = TestRapid()

    @Test
    fun `håndterer innsending_mottatt hendelser`() {
        val mockMediator = mockk<Mediator>(relaxed = true)
        val forventetDato = LocalDateTime.now()
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(søknadJson(forventetDato))
        verify(exactly = 1) {
            mockMediator.handle(
                SoknadMottattHendelse(
                    fodselsnummer = "123",
                    journalpostId = "123kkh",
                    brukerBehandlingsId = "123hurra",
                    datoRegistrert = forventetDato
                )
            )
        }
    }

    @Test
    fun `håndterer ikke papirsøknader`(){
        val mockMediator = mockk<Mediator>(relaxed = true)
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(papirsøknadJson)
        verify (exactly = 0){
            mockMediator.handle(any())
        }

    }

}

//language=json
private fun søknadJson(datoRegistrert: LocalDateTime) = """{
    
  "@event_name": "innsending_mottatt",
  "@opprettet": "${LocalDateTime.now()}",
  "fødselsnummer": "123",
  "journalpostId": "123kkh",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "datoRegistrert": "${datoRegistrert}",
  "søknadsData": {
    "brukerBehandlingId": "123hurra",
    "vedlegg": [],
    "skjemaNummer": "NAV12"
  }
}
""".trimIndent()

//language=json
private val papirsøknadJson = """{
  "@id": "123",
  "@opprettet": "2021-01-01T01:01:01.000001",
  "journalpostId": "12455",
  "datoRegistrert": "2021-01-01T01:01:01.000001",
  "skjemaKode": "NAV 03-102.23",
  "tittel": "Tittel",
  "type": "NySøknad",
  "fødselsnummer": "11111111111",
  "aktørId": "1234455",
  "søknadsData": {},
  "@event_name": "innsending_mottatt",
  "system_read_count": 0
}
""".trimIndent()