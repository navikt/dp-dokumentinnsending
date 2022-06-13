package no.nav.dagpenger.dokumentinnsending.modell

interface SoknadVisitor {
    fun visit(tilstand: Soknad.Tilstand, journalPostId: String, fodselsnummer: String, brukerbehandlingsId: String) {}
}
