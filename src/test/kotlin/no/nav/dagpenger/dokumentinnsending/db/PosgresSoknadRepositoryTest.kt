package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals

internal class PosgresSoknadRepositoryTest {

    @Test
    fun `lagrer og henter søknad`() {
        PostgresTestHelper.withMigratedDb { ds ->
            val soknad = Soknad(
                tilstand = Soknad.Mottatt,
                journalpostId = "456",
                fodselsnummer = "fnr",
                brukerbehandlingId = "123",
                vedlegg = mutableListOf(
                    Vedlegg("123", InnsendingStatus.INNSENDT),
                    Vedlegg("123", InnsendingStatus.IKKE_INNSENDT),
                    Vedlegg("123", InnsendingStatus.IKKE_INNSENDT),
                ),
                registrertDato = ZonedDateTime.now()
            )

            val repo = PostgresSoknadRepository(ds)
            repo.lagre(soknad)

            assertEquals(soknad, repo.hent("123"))
        }
    }

}
