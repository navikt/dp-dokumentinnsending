package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

interface SoknadVisitor {
    fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime,
        aktivitetslogg: Aktivitetslogg
    ) {
    }

    fun visitVedlegg(vedlegg: List<Vedlegg>) {}
}

interface VedleggVisitor {
    fun visit(
        status: InnsendingStatus,
        eksternSoknadId: String,
        journalPostId: String,
        navn: String,
        skjemakode: String,
        registrertDato: ZonedDateTime
    ) {
    }
}
