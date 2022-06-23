package no.nav.dagpenger.dokumentinnsending.modell.innsending

import java.util.UUID

interface InnsendingVisitor {
    fun visit(innsendingId: UUID, fodselsnummer: String) {}
}
