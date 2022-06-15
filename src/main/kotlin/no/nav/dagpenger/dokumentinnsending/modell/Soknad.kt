package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.AVVENTER_VEDLEGG
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.KOMPLETT
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.MOTTATT
import java.time.ZonedDateTime

class Soknad(
    private var tilstand: Tilstand = Mottatt,
    private val journalpostId: String,
    private val fodselsnummer: String,
    private val eksternSoknadId: String,
    private val registrertDato: ZonedDateTime,
    private val vedlegg: List<Vedlegg> = listOf()

) : Aktivitetskontekst {
    fun accept(visitor: SoknadVisitor) {
        visitor.visitVedlegg(vedlegg)
        visitor.visit(tilstand, journalpostId, fodselsnummer, eksternSoknadId, registrertDato)
    }

    fun handle(hendelse: SoknadMottattHendelse) {
        kontekst(hendelse, "Søknad motatt")
        tilstand.handle(this, hendelse)
    }

    fun handle(hendelse: EttersendingMottattHendelse) {
        kontekst(hendelse, "Ettersending motatt")
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

    // Gang of four State pattern
    interface Tilstand : Aktivitetskontekst {
        val type: SoknadTilstandType
        fun handle(soknad: Soknad, motattHendelse: SoknadMottattHendelse) {
            motattHendelse.warn("Forventet ikke SøknadMottatHendelse i ${this.type.name} tilstand")
        }

        fun handle(soknad: Soknad, ettersendingMottattHendelse: EttersendingMottattHendelse) {
            ettersendingMottattHendelse.warn("Forventet ikke EttersendingMotattHendelse i ${this.type.name} tilstand")
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

        override fun handle(soknad: Soknad, ettersendingMottattHendelse: EttersendingMottattHendelse) {
            if (soknad.erKomplett()) {
                soknad.tilstand(ettersendingMottattHendelse, Komplett)
            } else {
                soknad.tilstand(ettersendingMottattHendelse, AvventerVedlegg)
            }
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

        override fun handle(soknad: Soknad, ettersendingMottattHendelse: EttersendingMottattHendelse) {
            if (soknad.erKomplett()) {
                soknad.tilstand(ettersendingMottattHendelse, Komplett)
            } else {
                soknad.tilstand(ettersendingMottattHendelse, AvventerVedlegg)
            }
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
