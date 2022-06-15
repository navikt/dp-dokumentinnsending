package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import java.time.ZonedDateTime

internal fun lagSoknad(
    tilstand: Soknad.Tilstand = Soknad.Mottatt,
    journalpostId: String = "12345",
    fnr: String = "12345678910",
    brukerbehandlingId: String = "hjk",
    vedlegg: List<Vedlegg> = listOf(),
    registrertDato: ZonedDateTime = ZonedDateTime.now()

): Soknad {
    return Soknad(
        tilstand = tilstand,
        journalpostId = journalpostId,
        fodselsnummer = fnr,
        brukerbehandlingId = brukerbehandlingId,
        vedlegg = vedlegg,
        registrertDato = registrertDato
    )
}

internal fun lagInnsendtVedlegg(
    bbId: String = "123",
    jpId: String = "778821",
    navn: String = tilfeldigNavn(),
    skjemaKode: String = "T8",
    datoRegistrert: ZonedDateTime = ZonedDateTime.now()
) = Vedlegg(
    brukerbehandlingskjedeId = bbId,
    innsendingStatus = InnsendingStatus.INNSENDT,
    journalpostId = jpId,
    navn = navn,
    skjemaKode = skjemaKode,
    registrertDato = datoRegistrert
)

internal fun lagIkkeInnsendtVedlegg(
    bbId: String = "123",
    jpId: String = "778821",
    navn: String = tilfeldigNavn(),
    skjemaKode: String = "T8",
    datoRegistrert: ZonedDateTime = ZonedDateTime.now()
) = Vedlegg(
    brukerbehandlingskjedeId = bbId,
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
