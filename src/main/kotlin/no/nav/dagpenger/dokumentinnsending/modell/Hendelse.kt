package no.nav.dagpenger.dokumentinnsending.modell

abstract class Hendelse protected constructor(
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : IAktivitetslogg by aktivitetslogg, Aktivitetskontekst {

    abstract fun journalpostId(): String
    abstract fun fodselsnummer(): String
    abstract fun soknadBrukerbehandlingsId(): String

    init {
        aktivitetslogg.kontekst(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            SpesifikkKontekst(
                it,
                mapOf(
                    "journalpostId" to journalpostId(),
                    "brukerbehandlingsid" to soknadBrukerbehandlingsId()
                )
            )
        }
    }

    fun toLogString() = aktivitetslogg.toString()
}
