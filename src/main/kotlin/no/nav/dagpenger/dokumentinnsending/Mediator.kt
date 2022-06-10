package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository
import no.nav.dagpenger.dokumentinnsending.modell.Hendelse
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import org.slf4j.MDC

internal class Mediator(private val repository: SoknadRepository) {
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
            }
        } finally {
            MDC.clear()
        }
    }

    private fun soknad(hendelse: Hendelse): Soknad {
//       repository.hent(hendelse.journalpostId())
        return Soknad(
            journalPostId = hendelse.journalpostId(),
            soknadInternId = hendelse.soknadInternId()
        ) //toodo
    }
}