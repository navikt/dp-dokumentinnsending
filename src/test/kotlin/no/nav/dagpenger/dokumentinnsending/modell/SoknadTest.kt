package no.nav.dagpenger.dokumentinnsending.modell

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class SoknadTest {

    @Test
    fun erKomplett() {
        assertTrue(lagSoknad(vedlegg = listOf(lagVedlegg(innsendingStatus = InnsendingStatus.INNSENDT))).erKomplett())
        assertFalse(
            lagSoknad(
                vedlegg = listOf(
                    lagVedlegg(innsendingStatus = InnsendingStatus.INNSENDT),
                    lagVedlegg(innsendingStatus = InnsendingStatus.IKKE_INNSENDT)
                )
            ).erKomplett()
        )
    }

    @Test
    fun testEquals() {
        val vedleggliste = listOf(lagVedlegg(), lagVedlegg())
        assertEquals(lagSoknad(vedlegg = vedleggliste), lagSoknad(vedlegg = vedleggliste))
        assertNotEquals(lagSoknad(vedlegg = vedleggliste), lagSoknad(vedlegg = listOf(lagVedlegg())))
        assertNotEquals(
            lagSoknad(vedlegg = vedleggliste),
            lagSoknad(vedlegg = listOf(lagVedlegg(), lagVedlegg(innsendingStatus = InnsendingStatus.INNSENDT)))
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

private fun lagVedlegg(bid: String = "hjk", innsendingStatus: InnsendingStatus = InnsendingStatus.IKKE_INNSENDT) =
    Vedlegg(bid, innsendingStatus)
