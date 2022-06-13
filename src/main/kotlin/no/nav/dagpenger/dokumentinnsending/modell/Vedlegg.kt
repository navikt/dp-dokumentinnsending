package no.nav.dagpenger.dokumentinnsending.modell

class Vedlegg(private val brukerbehandlinskjedeId: String, private val innsendingStatus: InnsendingStatus) : Aktivitetskontekst {
    fun accept(visitor: VedleggVisitor){
        visitor.visit(this.innsendingStatus,this.brukerbehandlinskjedeId)
    }
    fun status()=innsendingStatus

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Vedleggskrav",
            mapOf("" to "")
        )
    }
}

enum class InnsendingStatus {
    INNSENDT,
    IKKE_INNSENDT
}
