import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import java.time.ZonedDateTime

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
