package no.nav.dagpenger.dokumentinnsending.modell

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal fun JsonNode.asZonedDateTime() = this.asLocalDateTime().atZone(ZoneId.of("Europe/Oslo"))

internal fun JsonMessage.datoRegistrert() = this["datoRegistrert"].asZonedDateTime()

internal fun JsonMessage.journalpostId(): String = this["journalpostId"].asText()

internal fun JsonMessage.brukerbehandlingId(): String = this["søknadsData.brukerBehandlingId"].asText()

internal fun JsonMessage.mottakLogMelding(): String = "Mottok melding om ${this["type"]} for søknad med journalpostId " +
    "${this.journalpostId()} og brukerbehandlingId ${this.brukerbehandlingId()}"

internal fun JsonMessage.vedlegg(
    brukerBehandlingId: String,
    journalpostId: String,
    registrertDato: ZonedDateTime
): List<Vedlegg> {
    return this["søknadsData.vedlegg"].map { node ->
        Vedlegg(
            eksternSoknadId = brukerBehandlingId,
            innsendingStatus = node["innsendingsvalg"].asText().let {
                when (it) {
                    "LastetOpp" -> InnsendingStatus.INNSENDT
                    else -> InnsendingStatus.IKKE_INNSENDT
                }
            },
            journalpostId = journalpostId,
            navn = node["navn"].asText(),
            skjemaKode = node["skjemaNummer"].asText(),
            registrertDato = registrertDato
        )
    }
}
