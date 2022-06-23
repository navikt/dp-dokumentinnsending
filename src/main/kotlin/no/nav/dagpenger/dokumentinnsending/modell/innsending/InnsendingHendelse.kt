package no.nav.dagpenger.dokumentinnsending.modell.innsending

import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetskontekst
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.IAktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst

abstract class InnsendingHendelse(private val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()) :
    IAktivitetslogg by aktivitetslogg,
    Aktivitetskontekst {
    init {
        aktivitetslogg.kontekst(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        TODO("Not yet implemented")
    }
}

class InnsendingStartetHendelse : InnsendingHendelse()
