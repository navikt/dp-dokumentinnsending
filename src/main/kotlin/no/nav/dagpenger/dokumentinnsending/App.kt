package no.nav.dagpenger.dokumentinnsending

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
            mediator = Mediator(InmemoryRepository())
        )
        EttersendingMottak(
            rapidsConnection = rapidsConnection
        )
    }

    fun start() = rapidsConnection.start()
}
