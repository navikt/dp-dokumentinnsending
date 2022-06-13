package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.modell.Soknad

interface SoknadRepository {
    fun lagre(soknad: Soknad)
    fun hent(soknadBrukerbehandlingId: String): Soknad?
}
