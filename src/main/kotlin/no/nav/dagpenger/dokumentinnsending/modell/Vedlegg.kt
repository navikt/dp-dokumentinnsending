package no.nav.dagpenger.dokumentinnsending.modell

class Vedlegg(val brukerbehandlinskjedeId: String, val innsendingStatus: InnsendingStatus) : Aktivitetskontekst {
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
