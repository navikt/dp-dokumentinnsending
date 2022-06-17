package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.modell.Aktivitetslogg
import no.nav.dagpenger.dokumentinnsending.modell.AktivitetsloggVisitor
import no.nav.dagpenger.dokumentinnsending.modell.Soknad
import no.nav.dagpenger.dokumentinnsending.modell.SoknadTilstandType
import no.nav.dagpenger.dokumentinnsending.modell.SoknadVisitor
import no.nav.dagpenger.dokumentinnsending.modell.SpesifikkKontekst
import no.nav.dagpenger.dokumentinnsending.modell.Vedlegg
import java.time.ZonedDateTime

internal class MediatorTestVistor(soknad: Soknad?) : SoknadVisitor {
    lateinit var journalPostId: String
    lateinit var fodselsnummer: String
    lateinit var eksternSoknadId: String
    lateinit var registrertDato: ZonedDateTime
    lateinit var tilstandType: SoknadTilstandType
    lateinit var aktivitetslogg: Aktivitetslogg
    var antallVedlegg: Int = 0

    init {
        require(soknad != null)
        soknad.accept(this)
    }

    override fun visit(
        tilstand: Soknad.Tilstand,
        journalPostId: String,
        fodselsnummer: String,
        eksternSoknadId: String,
        registrertDato: ZonedDateTime,
        aktivitetslogg: Aktivitetslogg
    ) {
        this.journalPostId = journalPostId
        this.fodselsnummer = fodselsnummer
        this.eksternSoknadId = eksternSoknadId
        this.registrertDato = registrertDato
        this.aktivitetslogg = aktivitetslogg
        this.tilstandType = tilstand.type
    }

    override fun visitVedlegg(vedlegg: List<Vedlegg>) {
        this.antallVedlegg = vedlegg.size
    }
}

internal class AktivitetsloggTestVisitor(aktivitetslogg: Aktivitetslogg) : AktivitetsloggVisitor {
    var antErrors = 0
    var antWarnings = 0
    var antInfo = 0
    var antSevere = 0
    val meldinger = mutableListOf<String>()
    init {
        aktivitetslogg.accept(this)
    }

    override fun visitError(
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Error,
        melding: String,
        tidsstempel: String
    ) {
        meldinger.add(melding)
        this.antErrors++
    }

    override fun visitInfo(
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Info,
        melding: String,
        tidsstempel: String
    ) {
        meldinger.add(melding)
        this.antInfo++
    }

    override fun visitWarn(
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Warn,
        melding: String,
        tidsstempel: String
    ) {
        meldinger.add(melding)
        this.antWarnings++
    }

    override fun visitSevere(
        kontekster: List<SpesifikkKontekst>,
        aktivitet: Aktivitetslogg.Aktivitet.Severe,
        melding: String,
        tidsstempel: String
    ) {
        meldinger.add(melding)
        this.antSevere++
    }
}
