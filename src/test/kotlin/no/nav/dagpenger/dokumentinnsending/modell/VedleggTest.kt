package no.nav.dagpenger.dokumentinnsending.modell

import lagIkkeInnsendtVedlegg
import lagInnsendtVedlegg
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class VedleggTest {

    @Test
    fun testEquals() {
        val v1 = lagInnsendtVedlegg(navn = "v1 navn")
        assertEquals(v1, lagInnsendtVedlegg(navn = "v1 navn"))
        assertNotEquals(v1, lagIkkeInnsendtVedlegg())
        assertNotEquals(v1, lagInnsendtVedlegg(bbId = "614982"))
        assertNotEquals(v1, lagInnsendtVedlegg(skjemaKode = "N6"))
    }
}
