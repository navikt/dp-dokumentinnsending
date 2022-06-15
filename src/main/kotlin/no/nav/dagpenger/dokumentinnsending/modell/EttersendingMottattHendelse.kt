package no.nav.dagpenger.dokumentinnsending.modell

class EttersendingMottattHendelse(
    private val vedlegg: List<Vedlegg>,
    journalpostId: String,
    eksternSoknadId: String,
) : Hendelse(
    journalpostId, eksternSoknadId
) {
    fun vedlegg() = vedlegg
}
