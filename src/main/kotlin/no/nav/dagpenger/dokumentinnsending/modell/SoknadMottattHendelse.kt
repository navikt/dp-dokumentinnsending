package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

class SoknadMottattHendelse(
    private val fodselsnummer: String,
    private val vedlegg: List<Vedlegg>,
    private val registrertDato: ZonedDateTime,
    eksternSoknadId: String,
    journalpostId: String
) : Hendelse(journalpostId = journalpostId, eksternSoknadId = eksternSoknadId) {

    fun vedlegg(): List<Vedlegg> = vedlegg
    fun fodselsnummer(): String = fodselsnummer
    fun registrertDato() = registrertDato
}
