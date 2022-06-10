package no.nav.dagpenger.dokumentinnsending.modell

import java.time.LocalDateTime
import java.util.UUID

data class SoknadMottattHendelse(
    private val fodselsnummer: String,// id fra dp-mottak
    private val journalpostId: String,
    private val datoRegistrert: LocalDateTime,
    private val brukerBehandlingId: String,
) : Hendelse() {
    override fun journalpostId(): String = journalpostId
    override fun fodselsnummer(): String = fodselsnummer
}
