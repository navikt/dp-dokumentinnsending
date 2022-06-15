package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagSoknad
import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import no.nav.dagpenger.dokumentinnsending.modell.VedleggVisitor
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class PosgresSoknadRepositoryTest {
    @Test
    fun `lagrer og henter søknad`() {
        val brukerbehandlingId = "123"
        val soknad = lagSoknad(eksternSoknadId = brukerbehandlingId)
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds)
            repo.lagre(soknad)
            assertSoknadEquals(soknad, repo.hent(brukerbehandlingId))
        }
    }

    @Test
    fun `Kan oppdatere søknad`() {
        val soknadBrukerbehandlingId = "123"
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds).also {
                it.lagre(
                    lagSoknad(
                        eksternSoknadId = soknadBrukerbehandlingId,
                        vedlegg = listOf(
                            lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                            lagIkkeInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        )
                    )
                )
            }

            assertFalse(repo.hent(soknadBrukerbehandlingId)?.erKomplett() ?: false)

            repo.lagre(
                lagSoknad(
                    eksternSoknadId = soknadBrukerbehandlingId,
                    vedlegg = listOf(
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                    )
                )
            )
            val actualSoknad = repo.hent("123")
            assertNotNull(actualSoknad)
            assertTrue(actualSoknad.erKomplett())
            SoknadTestVisitor(actualSoknad).also {
                assertEquals(3, it.vedlegg.size)
            }
        }
    }
}

private fun assertSoknadEquals(expected: Soknad, actual: Soknad?) {
    assertNotNull(actual)
    val actualSoknad = SoknadTestVisitor(actual)
    val expectedSoknad = SoknadTestVisitor(expected)
    assertEquals(expectedSoknad.fodselsnummer, actualSoknad.fodselsnummer)
    assertEquals(expectedSoknad.tilstand, actualSoknad.tilstand)
    assertEquals(
        expectedSoknad.registrertDato.truncatedTo(ChronoUnit.SECONDS),
        actualSoknad.registrertDato.truncatedTo(ChronoUnit.SECONDS)
    )
    assertEquals(expectedSoknad.brukerbehandlingsId, actualSoknad.brukerbehandlingsId)
    assertEquals(expectedSoknad.vedlegg, actualSoknad.vedlegg)
}

private class SoknadTestVisitor(soknad: Soknad) : SoknadVisitor, VedleggVisitor {
    lateinit var tilstand: Soknad.Tilstand
    lateinit var journalPostId: String
    lateinit var fodselsnummer: String
    lateinit var brukerbehandlingsId: String
    lateinit var registrertDato: ZonedDateTime
    val vedlegg = mutableListOf<VedleggTestData>()

    init {
        soknad.accept(visitor = this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime
    ) {
        this.tilstand = tilstand
        this.journalPostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.brukerbehandlingsId = eksternSoknadId
        this.registrertDato = registrertDato
    }

    override fun visitVedlegg(vedlegg: List<Vedlegg>) {
        vedlegg.forEach { v -> v.accept(this) }
    }

    override fun visit(
        status: InnsendingStatus,
        eksternSoknadId: String,
        journalPostId: String,
        navn: String,
        skjemakode: String,
        registrertDato: ZonedDateTime
    ) {

        this.vedlegg.add(
            VedleggTestData(
                status,
                eksternSoknadId,
                journalPostId,
                navn,
                skjemakode,
                registrertDato
            )
        )
    }
}

private data class VedleggTestData(
    val status: InnsendingStatus,
    val brukerbehandlinskjedeId: String,
    val journalPostId: String,
    val navn: String,
    val skjemakode: String,
    val registrertDato: ZonedDateTime
)
