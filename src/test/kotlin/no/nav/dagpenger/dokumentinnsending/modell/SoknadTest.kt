package no.nav.dagpenger.dokumentinnsending.modell

import lagIkkeInnsendtVedlegg
import lagInnsendtVedlegg
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class SoknadTest {

    @Test
    fun erKomplett() {
        assertTrue(lagSoknad(vedlegg = listOf(lagInnsendtVedlegg())).erKomplett())
        assertFalse(
            lagSoknad(
                vedlegg = listOf(
                    lagInnsendtVedlegg(), lagIkkeInnsendtVedlegg()
                )
            ).erKomplett()
        )
    }

    @Test
    fun testEquals() {
        val vedleggliste = listOf(lagInnsendtVedlegg(), lagInnsendtVedlegg())
        assertEquals(lagSoknad(vedlegg = vedleggliste), lagSoknad(vedlegg = vedleggliste))
        assertNotEquals(lagSoknad(vedlegg = vedleggliste), lagSoknad(vedlegg = listOf(lagInnsendtVedlegg())))
        assertNotEquals(
            lagSoknad(vedlegg = vedleggliste),
            lagSoknad(vedlegg = listOf(lagInnsendtVedlegg(), lagIkkeInnsendtVedlegg()))
        )
    }
}

private fun lagSoknad(
    tilstand: Soknad.Tilstand = Soknad.Mottatt,
    journalpostId: String = "anfkuh45",
    fnr: String = "12345678910",
    brukerbehandlingId: String = "hjk",
    vedlegg: List<Vedlegg> = listOf(),
    registrertDato: ZonedDateTime = ZonedDateTime.now()

): Soknad {
    return Soknad(
        tilstand = tilstand,
        journalpostId = journalpostId,
        fodselsnummer = fnr,
        brukerbehandlingId = brukerbehandlingId,
        vedlegg = vedlegg,
        registrertDato = registrertDato
    )
}
