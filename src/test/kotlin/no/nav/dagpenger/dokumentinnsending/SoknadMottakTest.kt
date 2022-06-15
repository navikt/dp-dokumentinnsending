package no.nav.dagpenger.dokumentinnsending

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class SoknadMottakTest {
    private val testRapid = TestRapid()

    @Test
    fun `håndterer innsending_mottatt hendelser`() {
        val hendelseSlot = slot<SoknadMottattHendelse>()
        val mockMediator = mockk<Mediator>(relaxed = true)
        val forventetDato = ZonedDateTime.now(ZoneId.of("Europe/Oslo"))
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(søknadJson(forventetDato.toLocalDateTime()))
        verify(exactly = 1) {
            mockMediator.handle(
                capture(hendelseSlot)
            )
        }
        val actual = hendelseSlot.captured
        assertEquals("123", actual.fodselsnummer())
        assertEquals("123kkh", actual.journalpostId())
        assertEquals("123hurra", actual.eksternSoknadId())
        assertEquals(forventetDato, actual.registrertDato())
        assertEquals(0, actual.vedlegg().size)
    }

    @Test
    fun `håndterer ikke papirsøknader`() {
        val hendelseSlot = slot<SoknadMottattHendelse>()
        val mockMediator = mockk<Mediator>(relaxed = true)
        SoknadMottak(testRapid, mockMediator)
        testRapid.sendTestMessage(papirsøknadJson)
        verify(exactly = 0) {
            mockMediator.handle(capture(hendelseSlot))
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
  "datoRegistrert": "$datoRegistrert",
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
