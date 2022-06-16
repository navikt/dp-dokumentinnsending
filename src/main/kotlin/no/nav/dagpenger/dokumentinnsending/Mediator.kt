package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.modell.EttersendingMottattHendelse
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

    fun handle(hendelse: EttersendingMottattHendelse) {
        try {
            handle(hendelse) { soknad ->
                soknad.handle(hendelse)
            }
        } catch (error: UkjentSoknadExcpetion) {
            sikkerlogg.warn(error.message)
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
        return when (hendelse) {
            is SoknadMottattHendelse -> {
                Soknad(
                    journalpostId = hendelse.journalpostId(),
                    fodselsnummer = hendelse.fodselsnummer(),
                    eksternSoknadId = hendelse.eksternSoknadId(),
                    vedlegg = hendelse.vedlegg(),
                    registrertDato = hendelse.registrertDato()
                )
            }
            else -> {
                soknadRepository.hent(hendelse.eksternSoknadId())
                    ?: throw UkjentSoknadExcpetion(
                        "Fant ikke søknad med journalpostId ${hendelse.journalpostId()} og brukerbehandlingsId ${hendelse.eksternSoknadId()} på ${hendelse::class.simpleName}"
                    )
            }
        }
    }

    private fun finalize(soknad: Soknad, hendelse: Hendelse) {
        soknadRepository.lagre(soknad)
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) return sikkerlogg.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
    }
}

internal class UkjentSoknadExcpetion(melding: String) : IllegalArgumentException(melding)
