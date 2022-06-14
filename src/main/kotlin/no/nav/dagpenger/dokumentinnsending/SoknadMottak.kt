package no.nav.dagpenger.dokumentinnsending

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.ZoneId

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
        sikkerlogg.info { "Mottok ny søknad" }

        val mottatHendelse = packet.toSoknadMottatHendelse()
        mediator.handle(mottatHendelse)
    }
}

private fun JsonMessage.toSoknadMottatHendelse(): SoknadMottattHendelse {
    val brukerBehandlingId = this["søknadsData.brukerBehandlingId"].asText()
    return SoknadMottattHendelse(
        fodselsnummer = this["fødselsnummer"].asText(),
        journalpostId = this["journalpostId"].asText(),
        datoRegistrert = this["datoRegistrert"].asZonedDateTime(),
        brukerBehandlingsId = brukerBehandlingId,
        vedlegg = this.vedlegg(brukerBehandlingId)
    )
}

private fun JsonNode.asZonedDateTime() = this.asLocalDateTime().atZone(ZoneId.of("Europe/Oslo"))

private fun JsonMessage.vedlegg(brukerBehandlingId: String): List<Vedlegg> {
    return this["søknadsData.vedlegg"].map { node ->
        Vedlegg(
            brukerbehandlingskjedeId = brukerBehandlingId,
            innsendingStatus = node["innsendingsvalg"].asText().let {
                when (it) {
                    "LastetOpp" -> InnsendingStatus.INNSENDT
                    else -> InnsendingStatus.IKKE_INNSENDT
                }
            }
        )
    }
}
