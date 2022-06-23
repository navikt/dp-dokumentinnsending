import InnsendingRepository.InnsendingData
import SerDer.toInnsending
import SerDer.toInnsendingData
import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.modell.innsending.Innsending
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingHendelse
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingStartetHendelse
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingVisitor
import no.nav.dagpenger.dokumentinnsending.sikkerlogg
import org.slf4j.MDC
import java.util.UUID

private val sikkerlogg = KotlinLogging.logger("tjenestekall")

class InnsendingMediator(
    private val repository: InnsendingRepository
) {
    fun handle(innsendingStartetHendelse: InnsendingStartetHendelse) {
        handle(innsendingStartetHendelse) { innsending ->
            innsending.handle(innsendingStartetHendelse)
        }
    }

    private fun handle(hendelse: InnsendingHendelse, handler: (Innsending) -> Unit) {
        try {
            MDC.put("innsendingId", hendelse.innsendingId().toString())
            innsending(hendelse).also { soknad ->
                handler(soknad)
                finalize(soknad, hendelse)
            }
        } finally {
            MDC.clear()
        }
    }

    private fun innsending(hendelse: InnsendingHendelse): Innsending {
        return repository.hent(hendelse.innsendingId())?.toInnsending() ?: Innsending(
            fodselsnummer = hendelse.fodselsnummer()
        ).also {
            repository.lagre(it.toInnsendingData())
        }
    }

    private fun finalize(innsending: Innsending, hendelse: InnsendingHendelse) {
        repository.lagre(innsending.toInnsendingData())
        if (!hendelse.hasMessages()) return
        if (hendelse.hasErrors()) return sikkerlogg.info("aktivitetslogg inneholder errors: ${hendelse.toLogString()}")
        sikkerlogg.info("aktivitetslogg inneholder meldinger: ${hendelse.toLogString()}")
    }
}

internal object SerDer {
    // todo put me somewhere nice
    fun InnsendingData.toInnsending(): Innsending {
        return Innsending(
            innsendingId = UUID.fromString(this.innsendingId),
            fodselsnummer = this.fodselsnummer,
        )
    }

    fun Innsending.toInnsendingData(): InnsendingData {
        val visitor = Visitor(this)
        return InnsendingData(
            innsendingId = visitor.innsendingId.toString(),
            fodselsnummer = visitor.fodselsnummer,
        )
    }

    private class Visitor(innsending: Innsending) : InnsendingVisitor {
        lateinit var innsendingId: UUID
        lateinit var fodselsnummer: String

        init {
            innsending.accept(this)
        }

        override fun visit(innsendingId: UUID, fodselsnummer: String) {
            this.innsendingId = innsendingId
            this.fodselsnummer = fodselsnummer
        }
    }
}

interface InnsendingRepository {
    fun hent(innsendingId: UUID): InnsendingData?
    fun lagre(innsending: InnsendingData): InnsendingData?

    data class InnsendingData(val innsendingId: String, val fodselsnummer: String)
}
