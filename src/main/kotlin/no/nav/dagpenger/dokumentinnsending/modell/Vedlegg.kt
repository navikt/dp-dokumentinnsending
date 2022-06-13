package no.nav.dagpenger.dokumentinnsending.modell

class Vedlegg(brukerbehandlinskjedeId:String, innsendingStatus: InnsendingStatus) : Aktivitetskontekst {
    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Vedleggskrav",
            mapOf("" to "")
        )
    }
}

internal typealias InnsendingStatus=Boolean