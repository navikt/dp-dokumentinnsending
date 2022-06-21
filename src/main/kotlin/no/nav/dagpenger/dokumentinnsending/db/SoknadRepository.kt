package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.modell.Soknad

internal interface SoknadRepository {
    fun lagre(soknad: Soknad)
    fun hent(eksternSoknadId: String): Soknad?
    fun hentSoknaderForPerson(fnr: String): List<SoknadData>
}
