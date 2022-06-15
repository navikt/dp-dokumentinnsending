package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagSoknad
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PosgresSoknadRepositoryTest {
    @Test
    fun `lagrer og henter søknad`() {
        val brukerbehandlingId = "123"
        val soknad = lagSoknad(brukerbehandlingId = brukerbehandlingId)
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds)
            repo.lagre(soknad)
            assertEquals(soknad, repo.hent(brukerbehandlingId))
        }
    }

    @Test
    fun `Kan oppdatere søknad`() {
        val soknadBrukerbehandlingId = "123"
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds).also {
                it.lagre(
                    lagSoknad(
                        brukerbehandlingId = soknadBrukerbehandlingId,
                        vedlegg = listOf(
                            lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                            lagIkkeInnsendtVedlegg(bbId = "123", jpId = "468"),
                        )
                    )
                )
            }

            assertFalse(repo.hent(soknadBrukerbehandlingId)?.erKomplett() ?: false)

            repo.lagre(
                lagSoknad(
                    brukerbehandlingId = soknadBrukerbehandlingId,
                    vedlegg = listOf(
                        lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                        lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                        lagInnsendtVedlegg(bbId = "123", jpId = "468"),
                    )
                )
            )
            assertTrue(repo.hent("123")?.erKomplett() ?: false)
        }
    }
}
