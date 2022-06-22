package no.nav.dagpenger.dokumentinnsending.api

import no.nav.dagpenger.dokumentinnsending.db.SoknadData
import no.nav.dagpenger.dokumentinnsending.db.VedleggData
import java.time.ZonedDateTime

internal data class SoknadResponse(
    val registrertDato: ZonedDateTime,
    val innsendingsId: Long,
    val vedlegg: List<VedleggResponse>
) {
    data class VedleggResponse(
        val navn: String,
        val status: String
    ) {
        companion object {
            fun from(data: VedleggData): VedleggResponse {
                return VedleggResponse(
                    navn = data.navn,
                    status = data.status
                )
            }
        }
    }

    companion object {
        fun from(data: SoknadData): SoknadResponse {
            return SoknadResponse(
                registrertDato = data.registrertDato,
                innsendingsId = data.internId,
                vedlegg = data.vedleggData.map(VedleggResponse::from)
            )
        }
    }
}
