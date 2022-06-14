package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.AVVENTER_VEDLEGG
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.KOMPLETT
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.MOTTATT
import java.time.ZonedDateTime

class Soknad(
    private var tilstand: Tilstand = Mottatt,
    private val journalpostId: String,
    private val fodselsnummer: String,
    private val brukerbehandlingId: String,
    private val registrertDato: ZonedDateTime,
    private val vedlegg: List<Vedlegg> = listOf()

) : Aktivitetskontekst {
    fun accept(visitor: SoknadVisitor) {
        visitor.visitVedlegg(vedlegg)
        visitor.visit(tilstand, journalpostId, fodselsnummer, brukerbehandlingId, registrertDato)
    }

    fun handle(hendelse: SoknadMottattHendelse) {
        kontekst(hendelse, "Søknad motatt")
        tilstand.handle(this, hendelse)
    }

    val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()

    private fun kontekst(hendelse: Hendelse, melding: String) {
        hendelse.kontekst(this)
        hendelse.kontekst(this.tilstand)
        hendelse.info(melding)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst(
        "Søknad",
        mapOf(
            "journalpostId" to journalpostId,
        )
    )

    fun erKomplett(): Boolean = !vedlegg.any { it.status() == InnsendingStatus.IKKE_INNSENDT }

    override fun equals(other: Any?): Boolean {
        return (other is Soknad) &&
            other.tilstand == this.tilstand &&
            other.journalpostId == this.journalpostId &&
            other.brukerbehandlingId == this.brukerbehandlingId
        //     other.registrertDato == this.registrertDato TODO: feiler pga millisekunder
    }

    // Gang of four State pattern
    interface Tilstand : Aktivitetskontekst {
        val type: SoknadTilstandType
        fun handle(soknad: Soknad, motattHendelse: SoknadMottattHendelse) {
            motattHendelse.warn("Forventet ikke SøknadMottatHendelse i ${this.type.name} tilstand")
        }
    }

    object Mottatt : Tilstand {
        override val type: SoknadTilstandType
            get() = MOTTATT

        override fun handle(soknad: Soknad, motattHendelse: SoknadMottattHendelse) {
            if (soknad.erKomplett()) {
                soknad.tilstand(motattHendelse, Komplett)
            } else {
                soknad.tilstand(motattHendelse, AvventerVedlegg)
            }
        }

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                kontekstType = "Tilstand",
                kontekstMap = mapOf(
                    "tilstand" to type.name
                )
            )
        }
    }

    object Komplett : Tilstand {
        override val type: SoknadTilstandType
            get() = KOMPLETT

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                kontekstType = "Tilstand",
                kontekstMap = mapOf(
                    "tilstand" to type.name
                )
            )
        }
    }

    object AvventerVedlegg : Tilstand {
        override val type: SoknadTilstandType
            get() = AVVENTER_VEDLEGG

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                kontekstType = "Tilstand",
                kontekstMap = mapOf(
                    "tilstand" to type.name
                )
            )
        }
    }

    private fun tilstand(
        hendelse: Hendelse,
        nyTilstand: Tilstand
    ) {
        if (tilstand == nyTilstand) {
            return // Already in this state => ignore
        }
        tilstand = nyTilstand
        hendelse.kontekst(tilstand)
    }
}
