package no.nav.dagpenger.dokumentinnsending

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetskontekst
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg.Aktivitet.Behov.Behovtype.NyJournalpost
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst
import no.nav.dagpenger.dokumentinnsending.modell.innsending.Innsending
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID

internal class BehovMediatorTest {
    private companion object {
        private const val testIdent = "12345678912"
        private val testInnsendingId = UUID.randomUUID()
        private lateinit var behovMediator: BehovMediator
    }

    private val testRapid = TestRapid()
    private lateinit var aktivitetslogg: Aktivitetslogg
    private val testInnsending = Innsending(
        innsendingId = testInnsendingId,
        fodselsnummer = testIdent
    )

    @BeforeEach
    fun setup() {
        aktivitetslogg = Aktivitetslogg()
        behovMediator = BehovMediator(
            rapidsConnection = testRapid,
        )
        testRapid.reset()
    }

    @Test
    internal fun `Behov blir sendt og inneholder det den skal`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.kontekst(testInnsending)
        hendelse.kontekst(Testkontekst("Testkontekst"))

        hendelse.behov(
            NyJournalpost,
            "Behøver jp",
            mapOf(
                "parameter1" to "verdi1",
                "parameter2" to "verdi2"
            )
        )

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        Assertions.assertEquals(1, inspektør.size)
        Assertions.assertEquals(testIdent, inspektør.key(0), "Forventer at partisjonsnøkker er ident ($testIdent)")
        inspektør.message(0).also { json ->
            assertStandardBehovFelter(json)
            Assertions.assertEquals(listOf("NyJournalpost"), json["@behov"].map(JsonNode::asText))
            Assertions.assertEquals(testIdent, json["fodselnummer"].asText())
            Assertions.assertEquals("Testkontekst", json["Testkontekst"].asText())
            Assertions.assertEquals("verdi1", json["parameter1"].asText())
            Assertions.assertEquals("verdi2", json["parameter2"].asText())
            Assertions.assertEquals("verdi1", json["NyJournalpost"]["parameter1"].asText())
            Assertions.assertEquals("verdi2", json["NyJournalpost"]["parameter2"].asText())
        }
    }

    @Test
    internal fun `Gruppere behov`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.kontekst(innsending = testInnsending)
        hendelse.kontekst(Testkontekst("Testkontekst"))

        hendelse.behov(
            NyJournalpost,
            "Trenger jp",
            mapOf(
                "parameter1" to "verdi1",
                "parameter2" to "verdi2"
            )
        )

        behovMediator.håndter(hendelse)

        val inspektør = testRapid.inspektør

        Assertions.assertEquals(1, inspektør.size)
        inspektør.message(0).also { json ->
            assertStandardBehovFelter(json)
            Assertions.assertEquals(listOf("NyJournalpost"), json["@behov"].map(JsonNode::asText))
            Assertions.assertEquals(testIdent, json["fodselnummer"].asText())
            Assertions.assertEquals("Testkontekst", json["Testkontekst"].asText())
            Assertions.assertEquals("verdi1", json["parameter1"].asText())
            Assertions.assertEquals("verdi2", json["parameter2"].asText())
            Assertions.assertEquals("verdi1", json["NyJournalpost"]["parameter1"].asText())
            Assertions.assertEquals("verdi2", json["NyJournalpost"]["parameter2"].asText())
        }
    }

    @Test
    internal fun `sjekker etter duplikatverdier`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.kontekst(testInnsending)
        hendelse.behov(
            NyJournalpost,
            "Behøver tom søknad for denne søknaden",
            mapOf(
                "ident" to testIdent
            )
        )
        hendelse.behov(
            NyJournalpost,
            "Behøver tom søknad for denne søknaden",
            mapOf(
                "ident" to testIdent
            )
        )

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    @Test
    internal fun `kan ikke produsere samme behov`() {
        val hendelse = TestHendelse("Hendelse1", aktivitetslogg.barn())
        hendelse.kontekst(innsending = testInnsending)
        hendelse.behov(NyJournalpost, "Behøver tom søknad for denne søknaden")
        hendelse.behov(NyJournalpost, "Behøver tom søknad for denne søknaden")

        assertThrows<IllegalArgumentException> { behovMediator.håndter(hendelse) }
    }

    private fun assertStandardBehovFelter(json: JsonNode) {
        Assertions.assertEquals("behov", json["@event_name"].asText())
        Assertions.assertTrue(json.hasNonNull("@id"))
        assertDoesNotThrow { UUID.fromString(json["@id"].asText()) }
        Assertions.assertTrue(json.hasNonNull("@opprettet"))
        assertDoesNotThrow { LocalDateTime.parse(json["@opprettet"].asText()) }
    }

    private class Testkontekst(
        private val melding: String
    ) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() = SpesifikkKontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        private val melding: String,
        val logg: Aktivitetslogg
    ) : InnsendingHendelse(testInnsendingId, testIdent, logg), Aktivitetskontekst {
        init {
            logg.kontekst(this)
        }

        override fun toSpesifikkKontekst() = SpesifikkKontekst("TestHendelse")
        override fun kontekst(kontekst: Aktivitetskontekst) {
            logg.kontekst(kontekst)
        }
    }
}
