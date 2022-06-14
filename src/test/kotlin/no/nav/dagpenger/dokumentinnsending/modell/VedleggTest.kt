package no.nav.dagpenger.dokumentinnsending.modell

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class VedleggTest {

    @Test
    fun testEquals() {
        val vedlegg1 = Vedlegg(brukerbehandlingskjedeId = "hfajk", innsendingStatus = InnsendingStatus.IKKE_INNSENDT)
        assertEquals(vedlegg1, Vedlegg(brukerbehandlingskjedeId = "hfajk", innsendingStatus = InnsendingStatus.IKKE_INNSENDT))
        assertNotEquals(vedlegg1, Vedlegg(brukerbehandlingskjedeId = "hfajk", innsendingStatus = InnsendingStatus.INNSENDT))
        assertNotEquals(vedlegg1, Vedlegg(brukerbehandlingskjedeId = "lahfl", innsendingStatus = InnsendingStatus.IKKE_INNSENDT))
    }
}
