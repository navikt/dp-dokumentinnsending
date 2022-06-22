package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import java.time.ZonedDateTime

internal data class SoknadMedVedlegg(
    val soknadId: Int,
    val antallVedlegg: Int
)

internal fun lagSoknader(fnr: String, vararg soknader: SoknadMedVedlegg): List<Soknad> {
    return soknader.map { soknadMedVedlegg ->
        lagSoknad(
            fnr = fnr,
            eksternSoknadId = soknadMedVedlegg.soknadId.toString(),
            journalpostId = soknadMedVedlegg.soknadId.toString(),
            vedlegg = (1..soknadMedVedlegg.antallVedlegg).map { i ->
                lagInnsendtVedlegg(navn = "$i")
            }
        )
    }
}

internal fun lagSoknad(
    tilstand: Soknad.Tilstand = Soknad.Mottatt,
    journalpostId: String = "12345",
    fnr: String = "12345678910",
    eksternSoknadId: String = "hjk",
    vedlegg: List<Vedlegg> = listOf(),
    registrertDato: ZonedDateTime = ZonedDateTime.now()

): Soknad {
    return Soknad(
        tilstand = tilstand,
        journalpostId = journalpostId,
        fodselsnummer = fnr,
        eksternSoknadId = eksternSoknadId,
        vedlegg = vedlegg,
        registrertDato = registrertDato
    )
}

internal fun lagInnsendtVedlegg(
    eksternSoknadId: String = "123",
    jpId: String = "778821",
    navn: String = tilfeldigNavn(),
    skjemaKode: String = "T8",
    datoRegistrert: ZonedDateTime = ZonedDateTime.now()
) = Vedlegg(
    eksternSoknadId = eksternSoknadId,
    innsendingStatus = InnsendingStatus.INNSENDT,
    journalpostId = jpId,
    navn = navn,
    skjemaKode = skjemaKode,
    registrertDato = datoRegistrert
)

internal fun lagIkkeInnsendtVedlegg(
    eksternSoknadId: String = "123",
    jpId: String = "778821",
    navn: String = tilfeldigNavn(),
    skjemaKode: String = "T8",
    datoRegistrert: ZonedDateTime = ZonedDateTime.now()
) = Vedlegg(
    eksternSoknadId = eksternSoknadId,
    innsendingStatus = InnsendingStatus.IKKE_INNSENDT,
    journalpostId = jpId,
    navn = navn,
    skjemaKode = skjemaKode,
    registrertDato = datoRegistrert
)

private fun tilfeldigNavn(): String {
    val charset = ('a'..'z') + ('A'..'Z') + (' ')
    return List(50) { charset.random() }
        .joinToString("")
}
