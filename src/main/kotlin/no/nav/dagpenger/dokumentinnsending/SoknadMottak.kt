package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import no.nav.dagpenger.dokumentinnsending.modell.brukerbehandlingId
import no.nav.dagpenger.dokumentinnsending.modell.datoRegistrert
import no.nav.dagpenger.dokumentinnsending.modell.journalpostId
import no.nav.dagpenger.dokumentinnsending.modell.mottakLogMelding
import no.nav.dagpenger.dokumentinnsending.modell.vedlegg
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class SoknadMottak(
    rapidsConnection: RapidsConnection,
    private val mediator: Mediator
) : River.PacketListener {

    companion object {
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.SoknadMottak")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "innsending_mottatt") }
            validate {
                it.requireKey(
                    "fødselsnummer",
                    "journalpostId",
                    "datoRegistrert",
                    "søknadsData.vedlegg",
                    "søknadsData.brukerBehandlingId"
                )
            }
            validate { it.requireAny("type", listOf("NySøknad", "Gjenopptak")) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val mottatHendelse = packet.toSoknadMottatHendelse()
        sikkerlogg.info(packet.mottakLogMelding())
        mediator.handle(mottatHendelse)
    }
}

private fun JsonMessage.toSoknadMottatHendelse(): SoknadMottattHendelse {
    val brukerBehandlingId = this.brukerbehandlingId()
    val journalpostId = this.journalpostId()
    val datoRegistrert = this.datoRegistrert()
    return SoknadMottattHendelse(
        fodselsnummer = this["fødselsnummer"].asText(),
        journalpostId = journalpostId,
        datoRegistrert = datoRegistrert,
        eksternSoknadId = brukerBehandlingId,
        vedlegg = this.vedlegg(brukerBehandlingId, journalpostId, datoRegistrert)
    )
}
