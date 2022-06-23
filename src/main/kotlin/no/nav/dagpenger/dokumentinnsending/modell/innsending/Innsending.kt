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
    private var journalpostId: String? = null,
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

        fun handle(innsending: Innsending, innsendingStartet: InnsendingStartetHendelse) =
            innsendingStartet.`kan ikke håndteres i denne tilstanden`()

        fun handle(
            innsending: Innsending,
            innsendingMidlertidigJournalført: InnsendingMidlertidigJournalførtHendelse
        ) =
            innsendingMidlertidigJournalført.`kan ikke håndteres i denne tilstanden`()

        fun handle(
            innsending: Innsending,
            innsendingJournalført: InnsendingJournalførtHendelse
        ) =
            innsendingJournalført.`kan ikke håndteres i denne tilstanden`()

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return this.javaClass.canonicalName.split('.').last().let {
                SpesifikkKontekst(it, emptyMap())
            }
        }

        private fun InnsendingHendelse.`kan ikke håndteres i denne tilstanden`() =
            this.warn("Kan ikke håndtere ${this.javaClass.simpleName} i tilstand $type")
    }

    private object Paabegynt : InnsendingsTilstand {
        override val type: InnsendingTilstandType = PÅBEGYNT
        override fun entering(innsendingHendelse: InnsendingHendelse, innsending: Innsending) {
            innsending.trengerNyJournalpost(innsendingHendelse)
        }

        override fun handle(innsending: Innsending, innsendingStartet: InnsendingStartetHendelse) {
            innsending.tilstand(innsendingStartet, AvventerMidlertidligJournalføring)
        }
    }

    private object AvventerMidlertidligJournalføring : InnsendingsTilstand {
        override val type = AVVENTER_MIDLERTIDIG_JOURNALFØRING
        override fun handle(
            innsending: Innsending,
            innsendingMidlertidigJournalført: InnsendingMidlertidigJournalførtHendelse
        ) {
            innsending.journalpostId = innsendingMidlertidigJournalført.journalPostId()
            innsending.tilstand(innsendingMidlertidigJournalført, AvventerJournalføring)
        }
    }

    private object AvventerJournalføring : InnsendingsTilstand {
        override val type = AVVENTER_JOURNALFØRING
        override fun handle(
            innsending: Innsending,
            innsendingJournalført: InnsendingJournalførtHendelse
        ) {
            innsending.tilstand(innsendingJournalført, Journalført)
        }
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

    private fun trengerNyJournalpost(innsendingHendelse: InnsendingHendelse) {
        innsendingHendelse.behov(
            type = Aktivitetslogg.Aktivitet.Behov.Behovtype.NyJournalpost,
            melding = "Trenger å journalføre innsending",
        )
    }
}

enum class InnsendingTilstandType() { PÅBEGYNT, AVVENTER_MIDLERTIDIG_JOURNALFØRING, AVVENTER_JOURNALFØRING, JOURNALFØRT }
