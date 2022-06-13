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

    init {
        rapidsConnection.register(this)
        SoknadMottak(
            rapidsConnection = rapidsConnection,
            mediator = Mediator(PostgresSoknadRepository())
        )
        EttersendingMottak(
            rapidsConnection = rapidsConnection
        )
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration(Configuration.dataSource)
    }
}
