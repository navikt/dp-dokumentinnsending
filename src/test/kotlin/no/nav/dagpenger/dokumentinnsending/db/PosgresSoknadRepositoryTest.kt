package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import org.junit.jupiter.api.Test

internal class PosgresSoknadRepositoryTest {

    @Test
    fun `lagre søknad`() {
        PostgresTestHelper.withMigratedDb {
            PostgresSoknadRepository(PostgresTestHelper.dataSource).lagre(
                Soknad(
                    tilstand = Soknad.Mottatt,
                    journalpostId = "123",
                    fodselsnummer = "fnr",
                    brukerbehandlingId = "bid",
                    vedlegg = mutableListOf(
                        Vedlegg("bid", InnsendingStatus.INNSENDT),
                        Vedlegg("bid", InnsendingStatus.IKKE_INNSENDT),
                    )
                )
            )
        }
    }
}
