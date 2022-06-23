package no.nav.dagpenger.dokumentinnsending.modell.innsending

import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetskontekst
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingTilstandType.AVVENTER_JOURNALFØRING
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingTilstandType.AVVENTER_MIDLERTIDIG_JOURNALFØRING
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingTilstandType.JOURNALFØRT
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingTilstandType.PÅBEGYNT
import java.util.UUID

class Innsending(
    private val innsendingId: UUID = UUID.randomUUID(),
    private val fodselsnummer: String,
    private var tilstand: InnsendingsTilstand = Paabegynt,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : Aktivitetskontekst {

    fun accept(visitor: InnsendingVisitor) {
        visitor.visit(innsendingId, fodselsnummer)
    }

    fun handle(innsendingStartet: InnsendingStartetHendelse) {
        tilstand.handle(this, innsendingStartet)
    }

    interface InnsendingsTilstand : Aktivitetskontekst {
        val type: InnsendingTilstandType
        fun entering(innsendingHendelse: InnsendingHendelse, innsending: Innsending) {}
        fun handle(innsending: Innsending, innsendingStartetHendelse: InnsendingStartetHendelse) {
            innsendingStartetHendelse.warn("Forventet ikke SøknadMottatHendelse i ${this.type.name} tilstand")
        }

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                kontekstType = "Tilstand",
                kontekstMap = mapOf(
                    "tilstand" to this.type.name
                )
            )
        }
    }

    private object Paabegynt : InnsendingsTilstand {
        override val type: InnsendingTilstandType = PÅBEGYNT

        override fun handle(innsending: Innsending, innsendingStartetHendelse: InnsendingStartetHendelse) {
            innsending.tilstand(innsendingStartetHendelse, AvventerMidlertidligJournalføring)
        }
    }

    private object AvventerMidlertidligJournalføring : InnsendingsTilstand {
        override val type = AVVENTER_MIDLERTIDIG_JOURNALFØRING
    }

    private object AvventerJournalføring : InnsendingsTilstand {
        override val type = AVVENTER_JOURNALFØRING
    }

    private object Journalført : InnsendingsTilstand {
        override val type = JOURNALFØRT
    }

    private fun tilstand(innsendingHendelse: InnsendingHendelse, nyTilstand: InnsendingsTilstand) {
        if (tilstand == nyTilstand) {
            return // Already in this state => ignore
        }
        this.tilstand = nyTilstand
        innsendingHendelse.kontekst(tilstand)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            kontekstType = "Innsending",
            kontekstMap = mapOf(
                "fodselnummer" to fodselsnummer,
                "innsendingId" to innsendingId.toString()
            )
        )
    }
}

enum class InnsendingTilstandType() { PÅBEGYNT, AVVENTER_MIDLERTIDIG_JOURNALFØRING, AVVENTER_JOURNALFØRING, JOURNALFØRT }
