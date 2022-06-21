package no.nav.dagpenger.dokumentinnsending.db

import no.nav.dagpenger.dokumentinnsending.lagIkkeInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagInnsendtVedlegg
import no.nav.dagpenger.dokumentinnsending.lagSoknad
import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadMottattHendelse
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import no.nav.dagpenger.dokumentinnsending.modell.VedleggVisitor
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class PosgresSoknadRepositoryTest {

    @Test
    fun `Hent soknad for bruker`() {
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds)
            repo.lagre(lagSoknad(fnr = "123", vedlegg = emptyList()))

            repo.hentSoknaderForPerson("123").let {
                assertEquals(1, it.size)
            }
        }
    }

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
        val eksternId = "123"
        PostgresTestHelper.withMigratedDb { ds ->
            val repo = PostgresSoknadRepository(ds).also {
                it.lagre(
                    lagSoknad(
                        eksternSoknadId = eksternId,
                        vedlegg = listOf(
                            lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                            lagIkkeInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        )
                    )
                )
            }

            val motattSoknad = repo.hent(eksternId)
            requireNotNull(motattSoknad)
            assertEquals(SoknadTilstandType.MOTTATT, SoknadTestVisitor(motattSoknad).tilstand.type)

            repo.lagre(
                lagSoknad(
                    eksternSoknadId = eksternId,
                    tilstand = Soknad.Komplett,
                    vedlegg = listOf(
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                        lagInnsendtVedlegg(eksternSoknadId = "123", jpId = "468"),
                    )
                )
            )
            val oppdatertSoknad = repo.hent("123")
            assertNotNull(oppdatertSoknad)
            SoknadTestVisitor(oppdatertSoknad).also {
                assertEquals(3, it.vedlegg.size)
                assertEquals(SoknadTilstandType.KOMPLETT, it.tilstand.type)
            }
        }
    }

    @Test
    fun `Aktivitets logg blir lagret`() {
        PostgresTestHelper.withMigratedDb { ds ->
            PostgresSoknadRepository(ds).also { repo ->
                repo.lagre(
                    lagSoknad(eksternSoknadId = "1", fnr = "1").also { soknad ->
                        soknad.handle(
                            SoknadMottattHendelse(
                                fodselsnummer = "1",
                                vedlegg = listOf(),
                                registrertDato = ZonedDateTime.now(),
                                eksternSoknadId = "1",
                                journalpostId = "1"
                            )
                        )
                    }
                )

                repo.hent("1").also {
                    requireNotNull(it)
                    SoknadTestVisitor(it).also { actual ->
                        assertEquals(1, actual.aktiviteter)
                    }
                }
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
    var aktiviteter: Int = 0

    init {
        soknad.accept(visitor = this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime,
        aktivitetslogg: Aktivitetslogg
    ) {
        this.tilstand = tilstand
        this.journalPostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.brukerbehandlingsId = eksternSoknadId
        this.registrertDato = registrertDato
        this.aktiviteter = aktivitetslogg.aktivitetsteller()
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
