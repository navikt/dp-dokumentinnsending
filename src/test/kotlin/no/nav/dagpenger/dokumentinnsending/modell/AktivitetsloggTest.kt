package no.nav.dagpenger.dokumentinnsending.modell

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AktivitetsloggTest {

    private lateinit var aktivitetslogg: Aktivitetslogg
    private lateinit var soknad: TestKontekst

    @BeforeEach
    fun setUp() {
        soknad = TestKontekst("Innsending")
        aktivitetslogg = Aktivitetslogg()
    }

    @Test
    fun `inneholder original melding`() {
        val infomelding = "info message"
        aktivitetslogg.info(infomelding)
        assertInfo(infomelding)
    }

    @Test
    fun `har ingen feil ved default`() {
        assertFalse(aktivitetslogg.hasErrors())
    }

    @Test
    fun `severe oppdaget og kaster exception`() {
        val melding = "Severe error"
        org.junit.jupiter.api.assertThrows<Aktivitetslogg.AktivitetException> { aktivitetslogg.severe(melding) }
        assertTrue(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertSevere(melding)
    }

    @Test
    fun `error oppdaget`() {
        val melding = "Error"
        aktivitetslogg.error(melding)
        assertTrue(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertError(melding)
    }

    @Test
    fun `warning oppdaget`() {
        val melding = "Warning explanation"
        aktivitetslogg.warn(melding)
        assertFalse(aktivitetslogg.hasErrors())
        assertTrue(aktivitetslogg.toString().contains(melding))
        assertWarn(melding)
    }

    @Test
    fun `Melding sendt til forelder`() {
        val hendelse = TestHendelse(
            "Hendelse",
            aktivitetslogg.barn()
        )
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
        "error message".also {
            hendelse.error(it)
            assertError(it, hendelse.logg)
            assertError(it, aktivitetslogg)
        }
    }

    @Test
    fun `Melding sendt fra barnebarn til forelder`() {
        val hendelse = TestHendelse(
            "Hendelse",
            aktivitetslogg.barn()
        )
        hendelse.kontekst(soknad)
        val arbeidsgiver =
            TestKontekst("Melding")
        hendelse.kontekst(arbeidsgiver)
        val vedtaksperiode =
            TestKontekst("Soknad")
        hendelse.kontekst(vedtaksperiode)
        "info message".also {
            hendelse.info(it)
            assertInfo(it, hendelse.logg)
            assertInfo(it, aktivitetslogg)
        }
        "error message".also {
            hendelse.error(it)
            assertError(it, hendelse.logg)
            assertError(it, aktivitetslogg)
            assertError("Hendelse", aktivitetslogg)
            assertError("Soknad", aktivitetslogg)
            assertError("Melding", aktivitetslogg)
            assertError("Innsending", aktivitetslogg)
        }
    }

    private fun assertInfo(message: String, aktivitetslogg: Aktivitetslogg = this.aktivitetslogg) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitInfo(
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Info,
                    melding: String,
                    tidsstempel: String
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            }
        )
        assertTrue(visitorCalled)
    }

    private fun assertWarn(message: String, aktivitetslogg: Aktivitetslogg = this.aktivitetslogg) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitWarn(
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Warn,
                    melding: String,
                    tidsstempel: String
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            }
        )
        assertTrue(visitorCalled)
    }

    private fun assertError(message: String, aktivitetslogg: Aktivitetslogg = this.aktivitetslogg) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitError(
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Error,
                    melding: String,
                    tidsstempel: String
                ) {
                    visitorCalled = true
                    assertTrue(message in aktivitet.toString(), aktivitetslogg.toString())
                }
            }
        )
        assertTrue(visitorCalled)
    }

    private fun assertSevere(message: String, aktivitetslogg: Aktivitetslogg = this.aktivitetslogg) {
        var visitorCalled = false
        aktivitetslogg.accept(
            object : AktivitetsloggVisitor {
                override fun visitSevere(
                    kontekster: List<SpesifikkKontekst>,
                    aktivitet: Aktivitetslogg.Aktivitet.Severe,
                    melding: String,
                    tidsstempel: String
                ) {
                    visitorCalled = true
                    assertEquals(message, melding)
                }
            }
        )
        assertTrue(visitorCalled)
    }

    private class TestKontekst(
        private val melding: String
    ) : Aktivitetskontekst {
        override fun toSpesifikkKontekst() = SpesifikkKontekst(melding, mapOf(melding to melding))
    }

    private class TestHendelse(
        private val melding: String,
        internal val logg: Aktivitetslogg
    ) : Aktivitetskontekst, IAktivitetslogg by logg {
        init {
            logg.kontekst(this)
        }

        override fun toSpesifikkKontekst() = SpesifikkKontekst("TestHendelse")
        override fun kontekst(kontekst: Aktivitetskontekst) {
            logg.kontekst(kontekst)
        }
    }
}