package no.nav.dagpenger.dokumentinnsending.db

data class AktivitetsloggData(
    val aktiviteter: List<AktivitetData>
) {
    data class AktivitetData(
        val alvorlighetsgrad: Alvorlighetsgrad,
        val label: Char,
        val behovtype: String?,
        val melding: String,
        val tidsstempel: String,
        val kontekster: List<SpesifikkKontekstData>,
        val detaljer: Map<String, Any>
    )

    data class SpesifikkKontekstData(
        val kontekstType: String,
        val kontekstMap: Map<String, String>
    )

    enum class Alvorlighetsgrad {
        INFO,
        WARN,
        BEHOV,
        ERROR,
        SEVERE
    }
}