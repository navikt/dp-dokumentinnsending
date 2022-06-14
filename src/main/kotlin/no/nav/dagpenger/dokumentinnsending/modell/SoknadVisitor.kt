package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

interface SoknadVisitor {
    fun visit(tilstand: Soknad.Tilstand, journalPostId: String, fodselsnummer: String, brukerbehandlingsId: String, registrertDato: ZonedDateTime) {}
    fun visitVedlegg(vedlegg: List<Vedlegg>) {}
}

interface VedleggVisitor {
    fun visit(status: InnsendingStatus, brukerbehandlinskjedeId: String) {}
}
