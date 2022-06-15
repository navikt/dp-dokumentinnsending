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
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.sql.DataSource

class PostgresSoknadRepository(private val dataSource: DataSource = Configuration.dataSource) : SoknadRepository {
    override fun lagre(soknad: Soknad) {
        val visitor = SoknadVisitor(soknad)
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                val internId = tx.run(
                    queryOf(
                        //language=PostgreSQL
                        """ INSERT INTO soknad_v1(journalpost_id,fodselnummer,ekstern_id,tilstand, registrert_dato) 
                            VALUES(:jpid,:fnr,:eid,:tilstand,:regdato) 
                            ON CONFLICT(ekstern_id) DO UPDATE SET sist_endret = :sistEndret 
                            RETURNING id 
                        """.trimIndent(),
                        mapOf(
                            "jpid" to visitor.journalpostId.toLong(),
                            "fnr" to visitor.fodselsnummer,
                            "eid" to visitor.eksternSoknadId,
                            "tilstand" to visitor.tilstand,
                            "regdato" to visitor.registrertDato,
                            "sistEndret" to ZonedDateTime.now(ZoneId.of("Europe/Oslo"))

                        )
                    ).map { row -> row.long("id") }.asSingle
                )!!

                tx.run(
                    queryOf(
                        //language=PostgreSQL
                        statement = """DELETE FROM vedlegg_v1 WHERE soknad_id=:soknadId""",
                        paramMap = mapOf("soknadId" to internId)
                    ).asExecute
                )

                tx.batchPreparedNamedStatement(
                    //language=PostgreSQL
                    """INSERT INTO vedlegg_v1(soknad_id,journalpost_id, status,registrert_dato,navn,skjemakode) 
                       VALUES(:internId, :jpid , :status, :regDato, :navn, :skjemakode)""".trimMargin(),
                    visitor.vedlegg.dbParametre(internId)

                )
            }
        }
    }

    override fun hent(eksternSoknadId: String): Soknad? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    "SELECT * FROM soknad_v1 WHERE ekstern_id=:eid",
                    mapOf(
                        "eid" to eksternSoknadId
                    )
                ).map { row ->
                    SoknadData(
                        internId = row.long("id"),
                        tilstand = row.string("tilstand"),
                        journalPostId = row.long("journalpost_id").toString(),
                        fnr = row.string("fodselnummer"),
                        eksternSoknadId = row.string("ekstern_id"),
                        registrertDato = row.zonedDateTime("registrert_dato")

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
                            eksternSoknadId = soknadData.eksternSoknadId,
                            journalpostId = row.string("journalpost_id"),
                            navn = row.string("navn"),
                            skjemaKode = row.string("skjemakode"),
                            registrertDato = row.zonedDateTime("registrert_dato")
                        )
                    }.asList
                )
                Soknad(
                    tilstand = soknadData.tilstandType(),
                    journalpostId = soknadData.journalPostId,
                    fodselsnummer = soknadData.fnr,
                    eksternSoknadId = soknadData.eksternSoknadId,
                    vedlegg = vedlegg,
                    registrertDato = soknadData.registrertDato
                )
            }
        }
    }

    private fun List<VedleggData>.dbParametre(internId: Long): List<Map<String, Any>> {
        return this.map {
            mapOf(
                "internId" to internId,
                "jpid" to it.journalPostId.toLong(),
                "status" to it.status,
                "regDato" to it.registrertDato,
                "navn" to it.navn,
                "skjemakode" to it.skjemakode
            )
        }
    }
}

private class SoknadVisitor(soknad: Soknad) :
    SoknadVisitor, VedleggVisitor {
    lateinit var tilstand: String
    lateinit var journalpostId: String
    lateinit var fodselsnummer: String
    lateinit var eksternSoknadId: String
    lateinit var registrertDato: ZonedDateTime
    val vedlegg = mutableListOf<VedleggData>()

    init {
        soknad.accept(this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime
    ) {
        this.tilstand = tilstand.type.name
        this.journalpostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.eksternSoknadId = eksternSoknadId
        this.registrertDato = registrertDato
    }

    override fun visitVedlegg(vedlegg: List<Vedlegg>) {
        vedlegg.forEach {
            it.accept(this)
        }
    }

    override fun visit(
        status: InnsendingStatus,
        eksternSoknadId: String,
        journalPostId: String,
        navn: String,
        skjemakode: String,
        registrertDato: ZonedDateTime
    ) {
        vedlegg.add(
            VedleggData(
                status = status.name,
                eksternSoknadId = eksternSoknadId,
                journalPostId = journalPostId,
                registrertDato = registrertDato,
                navn = navn,
                skjemakode = skjemakode
            )
        )
    }
}

private data class VedleggData(
    val status: String,
    val eksternSoknadId: String,
    val journalPostId: String,
    val registrertDato: ZonedDateTime,
    val navn: String,
    val skjemakode: String
)

private data class SoknadData(
    val internId: Long,
    val tilstand: String,
    val journalPostId: String,
    val fnr: String,
    val eksternSoknadId: String,
    val registrertDato: ZonedDateTime
) {
    fun tilstandType(): Soknad.Tilstand = when (SoknadTilstandType.valueOf(tilstand)) {
        SoknadTilstandType.MOTTATT -> Soknad.Mottatt
        SoknadTilstandType.KOMPLETT -> Soknad.Komplett
        SoknadTilstandType.AVVENTER_VEDLEGG -> Soknad.AvventerVedlegg
    }
}
