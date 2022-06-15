package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.modell.EttersendingMottattHendelse
import no.nav.dagpenger.dokumentinnsending.modell.brukerbehandlingId
import no.nav.dagpenger.dokumentinnsending.modell.journalpostId
import no.nav.dagpenger.dokumentinnsending.modell.mottakLogMelding
import no.nav.dagpenger.dokumentinnsending.modell.vedlegg
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class EttersendingMottak(rapidsConnection: RapidsConnection, private val mediator: Mediator) :
    River.PacketListener {
    companion object {
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.EttersendingMottak")
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
            validate { it.requireAny("type", listOf("Ettersending")) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info(packet.mottakLogMelding())
        mediator.handle(
            EttersendingMottattHendelse(
                vedlegg = packet.vedlegg(),
                journalpostId = packet.journalpostId(),
                eksternSoknadId = packet.brukerbehandlingId()

            )
        )
    }
}
