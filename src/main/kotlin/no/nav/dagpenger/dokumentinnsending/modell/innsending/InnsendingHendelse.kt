package no.nav.dagpenger.dokumentinnsending.modell.innsending

import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetskontekst
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.IAktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst
import java.util.UUID

abstract class InnsendingHendelse(
    private val innsendingId: UUID,
    private val fodselsnummer: String,
    private val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) :
    IAktivitetslogg by aktivitetslogg,
    Aktivitetskontekst {
    init {
        aktivitetslogg.kontekst(this)
    }

    fun innsendingId() = innsendingId
    fun fodselsnummer() = fodselsnummer

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        TODO("Not yet implemented")
    }

    fun toLogString() = aktivitetslogg.toString()
}

class InnsendingStartetHendelse(innsendingId: UUID, fodselsnummer: String) :
    InnsendingHendelse(innsendingId, fodselsnummer)

class InnsendingMidlertidigJournalførtHendelse(
    innsendingId: UUID,
    fodselsnummer: String,
    private val journalpostId: String,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : InnsendingHendelse(innsendingId, fodselsnummer) {
    fun journalPostId() = journalpostId
}

class InnsendingJournalførtHendelse(
    innsendingId: UUID,
    fodselsnummer: String,
    private val journalpostId: String,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : InnsendingHendelse(innsendingId, fodselsnummer) {
    fun journalPostId() = journalpostId
}
