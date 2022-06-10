package no.nav.dagpenger.dokumentinnsending

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class EttersendingMottak(rapidsConnection: RapidsConnection) : River.PacketListener {
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
                    "søknadsData.brukerBehandlingId",
                    "søknadsData.behandlingskjedeId",
                )
            }
            validate { it.requireAny("type", listOf("Ettersending")) }
            validate { it.interestedIn("søknadsData.vedlegg") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info { "Mottok ny ettersending" }
    }
}