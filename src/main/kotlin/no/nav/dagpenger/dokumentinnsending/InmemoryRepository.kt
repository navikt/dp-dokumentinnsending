package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.modell.Soknad

internal class InmemoryRepository : SoknadRepository {
    private val data = mutableMapOf<String, Soknad>()

    override fun lagre(soknad: Soknad) {
        data[soknad.brukerbehandlingId] = soknad
    }

    override fun hent(soknadBrukerbehandlingId: String): Soknad {
        return data[soknadBrukerbehandlingId]
            ?: throw IllegalArgumentException("Fant ikke søknad: $soknadBrukerbehandlingId")
    }
}
