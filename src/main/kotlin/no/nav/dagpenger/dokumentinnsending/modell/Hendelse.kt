package no.nav.dagpenger.dokumentinnsending.modell

abstract class Hendelse protected constructor(
    private val journalpostId: String,
    private val eksternSoknadId: String,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : IAktivitetslogg by aktivitetslogg, Aktivitetskontekst {

    fun journalpostId(): String = journalpostId
    fun eksternSoknadId(): String = eksternSoknadId

    init {
        aktivitetslogg.kontekst(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            SpesifikkKontekst(
                it,
                mapOf(
                    "journalpostId" to journalpostId(),
                    "eksternSøknadId" to eksternSoknadId()
                )
            )
        }
    }

    fun toLogString() = aktivitetslogg.toString()
}
