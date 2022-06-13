package no.nav.dagpenger.dokumentinnsending.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.dokumentinnsending.Configuration
import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import no.nav.dagpenger.dokumentinnsending.modell.VedleggVisitor
import javax.sql.DataSource

class PostgresSoknadRepository(private val dataSource: DataSource = Configuration.dataSource) : SoknadRepository {
    override fun lagre(soknad: Soknad) {
        val visitor = SoknadVisitor(soknad)
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val internId = tx.run(
                    queryOf(
                        """ INSERT INTO soknad_v1(journalpost_id,fodselnummer,brukerbehandling_id,tilstand) 
                        VALUES(:jpid,:fnr,:bid,:tilstand) RETURNING id 
                        """.trimIndent(),
                        mapOf(
                            "jpid" to visitor.journalpostId.toLong(),
                            "fnr" to visitor.fodselsnummer,
                            "bid" to visitor.brukerbehandlingId,
                            "tilstand" to visitor.tilstand
                        )
                    ).map { row -> row.long("id") }.asSingle
                )!!

                tx.batchPreparedNamedStatement(
                    "INSERT INTO vedlegg_v1(soknad_id, behandlingskjede_id,status) VALUES(:internId, :bhid, :status)",
                    visitor.vedlegg.dbParametre(internId)

                )
            }
        }
    }

    override fun hent(soknadBrukerbehandlingId: String): Soknad? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    "SELECT * FROM soknad_v1 WHERE brukerbehandling_id=:bid",
                    mapOf(
                        "bid" to soknadBrukerbehandlingId
                    )
                ).map { row ->
                    SoknadData(
                        internId = row.long("id"),
                        tilstand = row.string("tilstand"),
                        journalPostId = row.long("journalpost_id").toString(),
                        fnr = row.string("fodselnummer"),
                        brukerbehandlingId = row.string("brukerbehandling_id")
                    )
                }.asSingle
            )?.let { soknadData ->
                val vedlegg = session.run(
                    queryOf(
                        "SELECT * FROM vedlegg_v1 WHERE soknad_id=:soknadId",
                        mapOf("soknadId" to soknadData.internId)
                    ).map { row ->
                        Vedlegg(
                            innsendingStatus = InnsendingStatus.valueOf(row.string("status")),
                            brukerbehandlingskjedeId = row.string("behandlingskjede_id")

                        )
                    }.asList
                ).toMutableList()
                Soknad(
                    tilstand = soknadData.tilstandType(),
                    journalpostId = soknadData.journalPostId,
                    fodselsnummer = soknadData.fnr,
                    brukerbehandlingId = soknadData.brukerbehandlingId,
                    vedlegg = vedlegg
                )
            }
        }
    }

    private fun MutableList<VedleggData>.dbParametre(internId: Long): List<Map<String, Any>> {
        return this.map {
            mapOf("soknad_id" to internId, "bhid" to it.behandlingKjedeId, "status" to it.status)
        }
    }
}

private class SoknadVisitor(soknad: Soknad) :
    SoknadVisitor, VedleggVisitor {
    lateinit var tilstand: String
    lateinit var journalpostId: String
    lateinit var fodselsnummer: String
    lateinit var brukerbehandlingId: String
    val vedlegg = mutableListOf<VedleggData>()

    init {
        soknad.accept(this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        brukerbehandlingsId: String
    ) {
        this.tilstand = tilstand.type.name
        this.journalpostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.brukerbehandlingId = brukerbehandlingsId
    }

    override fun visitVedlegg(vedlegg: List<Vedlegg>) {
        vedlegg.forEach {
            it.accept(this)
        }
    }

    override fun visit(status: InnsendingStatus, brukerbehandlinskjedeId: String) {
        vedlegg.add(VedleggData(status.name, brukerbehandlinskjedeId))
    }
}

private data class VedleggData(val status: String, val behandlingKjedeId: String)

private data class SoknadData(
    val internId: Long,
    val tilstand: String,
    val journalPostId: String,
    val fnr: String,
    val brukerbehandlingId: String
) {
    fun tilstandType(): Soknad.Tilstand = when (SoknadTilstandType.valueOf(tilstand)) {
        SoknadTilstandType.MOTTATT -> Soknad.Mottatt
        SoknadTilstandType.KOMPLETT -> Soknad.Komplett
        SoknadTilstandType.AVVENTER_VEDLEGG -> Soknad.AvventerVedlegg
    }
}
