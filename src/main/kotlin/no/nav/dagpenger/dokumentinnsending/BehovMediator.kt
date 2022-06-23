package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.innsending.InnsendingHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

private val sikkerLogg = KotlinLogging.logger("BehovMediator")

class BehovMediator(
    private val rapidsConnection: RapidsConnection,
) {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    internal fun håndter(hendelse: InnsendingHendelse) {
        hendelse.kontekster().forEach { if (!it.hasErrors()) håndter(hendelse, it.behov()) }
    }

    private fun håndter(
        hendelse: InnsendingHendelse,
        behov: List<Aktivitetslogg.Aktivitet.Behov>
    ) {
        behov
            .groupBy { it.kontekst() }
            .onEach { (_, behovMap) ->
                require(
                    behovMap.size == behovMap.map { it.type.name }
                        .toSet().size
                ) { "Kan ikke produsere samme behov på samme kontekst" }
            }
            .forEach { (kontekst, behov) ->
                val behovMap: Map<String, Map<String, Any>> =
                    behov.associate { behov -> behov.type.name to behov.detaljer() }
                val behovParametere =
                    behovMap.values.fold<Map<String, Any>, Map<String, Any>>(emptyMap()) { acc, map -> acc + map }
                (kontekst + behovMap + behovParametere).let { JsonMessage.newNeed(behovMap.keys, it) }
                    .also { message ->
                        sikkerLogg.info("sender behov for {}:\n{}", behovMap.keys, message.toJson())
                        rapidsConnection.publish(hendelse.fodselsnummer(), message.toJson())
                        logger.info("Sender behov for {}", behovMap.keys)
                    }
            }
    }
}
