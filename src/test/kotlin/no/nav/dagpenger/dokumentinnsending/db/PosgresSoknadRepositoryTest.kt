package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

internal class PosgresSoknadRepositoryTest {

    @Test
    fun `lagre søknad`() {
        PostgresTestHelper.withMigratedDb {
            val repo = PostgresSoknadRepository(PostgresTestHelper.dataSource)
            repo.lagre(
                Soknad(
                    tilstand = Soknad.Mottatt,
                    journalpostId = "456",
                    fodselsnummer = "fnr",
                    brukerbehandlingId = "123",
                    vedlegg = mutableListOf(
                        Vedlegg("123", InnsendingStatus.INNSENDT),
                        Vedlegg("123", InnsendingStatus.IKKE_INNSENDT),
                    )
                )
            )

            repo.hent("123").also {
                assertNotNull(it)
                assertFalse(it.erKomplett())
            }
        }
    }
}
