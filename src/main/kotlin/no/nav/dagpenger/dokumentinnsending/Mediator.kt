package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.modell.Hendelse
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import org.slf4j.MDC

private val sikkerlogg = KotlinLogging.logger("tjenestekall")
internal class Mediator(private val soknadRepository: SoknadRepository) {
    fun handle(hendelse: SoknadMottattHendelse) {
        handle(hendelse) { soknad ->
            soknad.handle(hendelse)
        }
    }

    private fun handle(hendelse: Hendelse, handler: (Soknad) -> Unit) {
        try {
            MDC.put("journalpostId", hendelse.journalpostId())
            soknad(hendelse).also { soknad ->
                handler(soknad)
                finalize(soknad, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun soknad(hendelse: Hendelse): Soknad {
        // hente eller lage
        // hvis hente: søknad har allerede vedlegg --> liste skal ikke endres
        return Soknad(
            journalPostId = hendelse.journalpostId(),
            fodselsnummer = hendelse.fodselsnummer(),
            brukerbehandlingsId = hendelse.soknadBrukerbehandlingsId()
        )
    }

    private fun finalize(soknad: Soknad, hendelse: Hendelse) {
        soknadRepository.lagre(soknad)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) return sikkerlogg.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
    }
}
