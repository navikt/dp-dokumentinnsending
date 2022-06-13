package no.nav.dagpenger.dokumentinnsending.modell

import java.time.LocalDateTime

data class SoknadMottattHendelse(
    private val fodselsnummer: String,// id fra dp-mottak
    private val journalpostId: String,
    private val datoRegistrert: LocalDateTime,
    private val brukerBehandlingsId: String,
) : Hendelse() {
    override fun journalpostId(): String = journalpostId
    override fun fodselsnummer(): String = fodselsnummer
    override fun søknadBrukerbehandlingsId(): String = brukerBehandlingsId
    fun vedlegg(): List<Vedlegg> {
        TODO("Not yet implemented")
    }
}
