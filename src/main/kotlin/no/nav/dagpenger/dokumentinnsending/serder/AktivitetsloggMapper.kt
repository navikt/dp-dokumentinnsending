package no.nav.dagpenger.dokumentinnsending.serder

import no.nav.dagpenger.dokumentinnsending.db.AktivitetsloggData
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst

internal fun konverterTilAktivitetslogg(aktivitetsloggData: AktivitetsloggData): Aktivitetslogg =
    Aktivitetslogg().also { aktivitetslogg ->
        aktivitetsloggData.aktiviteter.forEach {
            val kontekster = it.kontekster.map { spesifikkKontekstData ->
                SpesifikkKontekst(
                    spesifikkKontekstData.kontekstType,
                    spesifikkKontekstData.kontekstMap
                )
            }
            aktivitetslogg.leggTil(
                when (it.alvorlighetsgrad) {
                    AktivitetsloggData.Alvorlighetsgrad.INFO -> Aktivitetslogg.Aktivitet.Info(
                        kontekster,
                        it.melding,
                        it.tidsstempel
                    )
                    AktivitetsloggData.Alvorlighetsgrad.WARN -> Aktivitetslogg.Aktivitet.Warn(
                        kontekster,
                        it.melding,
                        it.tidsstempel
                    )
                    AktivitetsloggData.Alvorlighetsgrad.ERROR -> Aktivitetslogg.Aktivitet.Error(
                        kontekster,
                        it.melding,
                        it.tidsstempel
                    )
                    AktivitetsloggData.Alvorlighetsgrad.SEVERE -> Aktivitetslogg.Aktivitet.Severe(
                        kontekster,
                        it.melding,
                        it.tidsstempel
                    )
                }
            )
        }
    }
