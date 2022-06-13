package no.nav.dagpenger.dokumentinnsending.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.dokumentinnsending.Configuration
import no.nav.dagpenger.dokumentinnsending.modell.InnsendingStatus
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
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

    override fun hent(soknadBrukerbehandlingId: String): Soknad {
        TODO("Not yet implemented")
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
        vedlegg.add(VedleggData(status.name,brukerbehandlinskjedeId))
    }
}

data class VedleggData(val status: String, val behandlingKjedeId: String)
