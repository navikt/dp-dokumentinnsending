package no.nav.dagpenger.dokumentinnsending.modell

import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType.MOTTATT
import java.util.UUID

class Soknad(
    private var tilstand: Tilstand = Mottatt,
    private val journalPostId: String,
    private val fodselsnummer: String

) : Aktivitetskontekst {
    fun handle(hendelse: SoknadMottattHendelse) {
        TODO("Not yet implemented")
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
            "journalpostId" to journalPostId,
        )
    )

    // Gang of four State pattern
    interface Tilstand : Aktivitetskontekst {
        val type: SoknadTilstandType
    }

    internal object Mottatt : Tilstand {
        override val type: SoknadTilstandType
            get() = MOTTATT

        override fun toSpesifikkKontekst(): SpesifikkKontekst {
            return SpesifikkKontekst(
                kontekstType = "Tilstand",
                kontekstMap = mapOf(
                    "tilstand" to type.name
                )
            )
        }
    }
}
