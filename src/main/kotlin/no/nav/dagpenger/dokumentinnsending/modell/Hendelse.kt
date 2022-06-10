package no.nav.dagpenger.dokumentinnsending.modell

import java.util.UUID

abstract class Hendelse protected constructor(
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : IAktivitetslogg by aktivitetslogg, Aktivitetskontekst {

    abstract fun journalpostId(): String
    abstract fun soknadInternId(): UUID

    init {
        aktivitetslogg.kontekst(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            SpesifikkKontekst(
                it, mapOf(
                    "journalpostId" to journalpostId(),
                    "søknadInternId" to soknadInternId().toString()
                )
            )
        }
    }

    fun toLogString() = aktivitetslogg.toString()
}
