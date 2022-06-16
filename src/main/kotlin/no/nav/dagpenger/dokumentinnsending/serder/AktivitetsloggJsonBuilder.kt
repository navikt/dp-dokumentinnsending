package no.nav.dagpenger.dokumentinnsending.serder

import no.nav.dagpenger.dokumentinnsending.db.AktivitetsloggData
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.AktivitetsloggVisitor
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst

class AktivitetsloggJsonBuilder(aktivitetslogg: Aktivitetslogg) {

    private val aktiviteter = Aktivitetslogginspektør(aktivitetslogg).aktiviteter

    internal fun toJson() = JsonMapper.jacksonJsonAdapter.writeValueAsString(
        mutableMapOf(
            "aktiviteter" to aktiviteter
        )
    )

    private inner class Aktivitetslogginspektør(aktivitetslogg: Aktivitetslogg) : AktivitetsloggVisitor {
        internal val aktiviteter = mutableListOf<Map<String, Any>>()

        init {
            aktivitetslogg.accept(this)
        }

        override fun visitInfo(
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitetslogg.Aktivitet.Info,
            melding: String,
            tidsstempel: String
        ) {
            leggTilMelding(kontekster, AktivitetsloggData.Alvorlighetsgrad.INFO, melding, tidsstempel)
        }

        override fun visitWarn(
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitetslogg.Aktivitet.Warn,
            melding: String,
            tidsstempel: String
        ) {
            leggTilMelding(kontekster, AktivitetsloggData.Alvorlighetsgrad.WARN, melding, tidsstempel)
        }

        override fun visitError(
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitetslogg.Aktivitet.Error,
            melding: String,
            tidsstempel: String
        ) {
            leggTilMelding(kontekster, AktivitetsloggData.Alvorlighetsgrad.ERROR, melding, tidsstempel)
        }

        override fun visitSevere(
            kontekster: List<SpesifikkKontekst>,
            aktivitet: Aktivitetslogg.Aktivitet.Severe,
            melding: String,
            tidsstempel: String
        ) {
            leggTilMelding(kontekster, AktivitetsloggData.Alvorlighetsgrad.SEVERE, melding, tidsstempel)
        }

        private fun leggTilMelding(
            kontekster: List<SpesifikkKontekst>,
            alvorlighetsgrad: AktivitetsloggData.Alvorlighetsgrad,
            melding: String,
            tidsstempel: String
        ) {
            aktiviteter.add(
                mutableMapOf<String, Any>(
                    "kontekster" to map(kontekster),
                    "alvorlighetsgrad" to alvorlighetsgrad.name,
                    "melding" to melding,
                    "detaljer" to emptyMap<String, Any>(),
                    "tidsstempel" to tidsstempel
                )
            )
        }

        private fun map(kontekster: List<SpesifikkKontekst>): List<Map<String, Any>> {
            return kontekster.map {
                mutableMapOf(
                    "kontekstType" to it.kontekstType,
                    "kontekstMap" to it.kontekstMap
                )
            }
        }
    }
}
