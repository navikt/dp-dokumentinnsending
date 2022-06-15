package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
