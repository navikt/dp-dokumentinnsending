package no.nav.dagpenger.dokumentinnsending.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.dokumentinnsending.Configuration
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import javax.sql.DataSource

class PostgresSoknadRepository(private val dataSource: DataSource = Configuration.dataSource) : SoknadRepository {
    override fun lagre(soknad: Soknad) {
        val visitor = SoknadVisitor(soknad)
        val internId = using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.run(
                    queryOf(
                        """ INSERT INTO soknad_v1(journalpostId,fodselnummer,brukerbehandlingId,tilstand) 
                        VALUES(:jpid,:fnr,:bid,:tilstand) RETURNING id 
                        """.trimIndent(),
                        mapOf(
                            "jpid" to visitor.journalpostId.toLong(),
                            "fnr" to visitor.fodselsnummer,
                            "bid" to visitor.brukerbehandlingId,
                            "tilstand" to visitor.tilstand
                        )
                    ).map { row -> row.long("id") }.asSingle
                )
            }
        }
    }

    override fun hent(soknadBrukerbehandlingId: String): Soknad {
        TODO("Not yet implemented")
    }
}

private class SoknadVisitor(soknad: Soknad) :
    SoknadVisitor {
    lateinit var tilstand: String
    lateinit var journalpostId: String
    lateinit var fodselsnummer: String
    lateinit var brukerbehandlingId: String

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
}
