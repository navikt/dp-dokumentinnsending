package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

data class SoknadMottattHendelse(
    private val fodselsnummer: String, // id fra dp-mottak
    private val journalpostId: String,
    private val datoRegistrert: ZonedDateTime,
    private val brukerBehandlingsId: String,
    private val vedlegg: List<Vedlegg>
) : Hendelse() {
    override fun journalpostId(): String = journalpostId
    override fun fodselsnummer(): String = fodselsnummer
    override fun soknadBrukerbehandlingsId(): String = brukerBehandlingsId
    fun vedlegg(): List<Vedlegg> = vedlegg
    fun datoRegistrert() = datoRegistrert
}
