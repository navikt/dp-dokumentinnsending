package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PosgresSoknadRepositoryTest {
    val registrertDato = ZonedDateTime.now()
    val vedleggListe = mutableListOf(
        lagInnsendtVedlegg(jpId = "456", bbId = "123", datoRegistrert = registrertDato),
        lagInnsendtVedlegg(jpId = "456", bbId = "123", datoRegistrert = registrertDato),
        lagIkkeInnsendtVedlegg(
            jpId = "456",
            bbId = "123",
            datoRegistrert = registrertDato
        )
    )
    private val soknad = Soknad(
        tilstand = Soknad.Mottatt,
        journalpostId = "456",
        fodselsnummer = "fnr",
        brukerbehandlingId = "123",
        vedlegg = vedleggListe,
        registrertDato = registrertDato
    )

    @Test
    fun `lagrer og henter søknad`() {
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds)
            repo.lagre(soknad)
            assertEquals(soknad, repo.hent("123"))
        }
    }

    @Test
    fun `Kan oppdatere søknad`() {

        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds).also { it.lagre(soknad) }
            repo.lagre(
                Soknad(
                    tilstand = Soknad.Mottatt,
                    journalpostId = "486",
                    fodselsnummer = "fnr",
                    brukerbehandlingId = "123",
                    vedlegg = mutableListOf(
                        lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                        lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                        lagInnsendtVedlegg(bbId = "123", jpId = "468")
                    ),
                    registrertDato = registrertDato
                )
            )
            assertTrue(repo.hent("123")?.erKomplett() ?: false)
        }
    }
}
