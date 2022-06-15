package no.nav.dagpenger.dokumentinnsending

import no.nav.dagpenger.dokumentinnsending.db.PostgresSoknadRepository
import no.nav.dagpenger.dokumentinnsending.db.runMigration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    App.start()
}

internal object App : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.create(Configuration.config)
    private val mediator = Mediator(PostgresSoknadRepository())

    init {
        rapidsConnection.register(this)
        SoknadMottak(
            rapidsConnection = rapidsConnection,
            mediator = mediator
        )
        EttersendingMottak(
            rapidsConnection = rapidsConnection,
            mediator = mediator
        )
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration(Configuration.dataSource)
    }
}
