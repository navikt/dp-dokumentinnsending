package no.nav.dagpenger.dokumentinnsending.modell

import java.time.ZonedDateTime

class Vedlegg(
    private val eksternSoknadId: String,
    private val innsendingStatus: InnsendingStatus,
    private val journalpostId: String,
    private val navn: String,
    private val skjemaKode: String,
    private val registrertDato: ZonedDateTime
) :
    Aktivitetskontekst {
    fun accept(visitor: VedleggVisitor) {
        visitor.visit(
            this.innsendingStatus,
            this.eksternSoknadId,
            this.journalpostId, this.navn, this.skjemaKode, this.registrertDato
        )
    }

    fun status() = innsendingStatus

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Vedlegg",
            mapOf(
                "eksternSoknadId" to eksternSoknadId,
                "status" to innsendingStatus.name,
                "journalpostId" to journalpostId
            )
        )
    }
}

enum class InnsendingStatus {
    INNSENDT,
    IKKE_INNSENDT
}
