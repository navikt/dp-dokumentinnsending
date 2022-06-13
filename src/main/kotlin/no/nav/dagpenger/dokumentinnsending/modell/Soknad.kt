package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.AVVENTER_VEDLEGG
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.KOMPLETT
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.MOTTATT

class Soknad(
    private var tilstand: Tilstand = Mottatt,
    private val journalpostId: String,
    private val fodselsnummer: String,
    private val brukerbehandlingId: String,
    private val vedlegg: MutableList<Vedlegg> = mutableListOf()

) : Aktivitetskontekst {
    fun accept(visitor: SoknadVisitor) {
        visitor.visitVedlegg(vedlegg)
        visitor.visit(tilstand, journalpostId, fodselsnummer, brukerbehandlingId)
    }

    fun handle(hendelse: SoknadMottattHendelse) {
        kontekst(hendelse, "Søknad motatt")
        vedlegg.addAll(hendelse.vedlegg())
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

    fun erKomplett(): Boolean = vedlegg.all { it.status() == InnsendingStatus.INNSENDT }

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
