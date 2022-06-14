package no.nav.dagpenger.dokumentinnsending.modell

class Vedlegg(
    private val brukerbehandlingskjedeId: String,
    private val innsendingStatus: InnsendingStatus,
    private val journalpostId: String,
) :
    Aktivitetskontekst {
    fun accept(visitor: VedleggVisitor) {
        visitor.visit(this.innsendingStatus, this.brukerbehandlingskjedeId, this.journalpostId)
    }

    fun status() = innsendingStatus

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Vedleggs",
            mapOf(
                "brukerbehandlingskjedeId" to brukerbehandlingskjedeId,
                "status" to innsendingStatus.name,
                "journalpostId" to journalpostId
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is Vedlegg &&
            other.brukerbehandlingskjedeId == this.brukerbehandlingskjedeId &&
            other.innsendingStatus == this.innsendingStatus &&
            other.journalpostId == this.journalpostId
    }
}

enum class InnsendingStatus {
    INNSENDT,
    IKKE_INNSENDT
}
