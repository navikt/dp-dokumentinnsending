package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

class SoknadMottattHendelse(
    private val fodselsnummer: String,
    private val datoRegistrert: ZonedDateTime,
    private val vedlegg: List<Vedlegg>,
    eksternSoknadId: String,
    journalpostId: String,
) : Hendelse(journalpostId = journalpostId, eksternSoknadId = eksternSoknadId) {

    fun vedlegg(): List<Vedlegg> = vedlegg
    fun datoRegistrert() = datoRegistrert
    fun fodselsnummer(): String = fodselsnummer
}
