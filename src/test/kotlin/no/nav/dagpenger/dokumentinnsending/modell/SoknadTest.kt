package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagSoknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
}
