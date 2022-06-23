package no.nav.dagpenger.dokumentinnsending.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.dokumentinnsending.Configuration
import no.nav.dagpenger.dokumentinnsending.auth.fnr
import no.nav.dagpenger.dokumentinnsending.auth.jwt
import no.nav.dagpenger.dokumentinnsending.db.SoknadRepository

internal fun Application.vedleggApi(soknadRepository: SoknadRepository) {

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }
    install(Authentication) {
        jwt(Configuration.AzureAd.name, Configuration.AzureAd.wellKnownUrl) {
            withAudience(Configuration.AzureAd.audience)
        }
        jwt(Configuration.TokenX.name, Configuration.TokenX.wellKnownUrl) {
            withAudience(Configuration.TokenX.audience)
        }
    }

    routing {
        route("/v1") {
            authenticate(Configuration.TokenX.name) {
                route("/vedlegg/{soknadId}") {
                    get {
                        call.parameters["soknadId"]?.let { soknadId ->
                            soknadRepository.hent(soknadId)?.let { soknad ->
                                call.respond(HttpStatusCode.OK, soknad)
                            } ?: call.respond(HttpStatusCode.NotFound)
                        } ?: call.respond(HttpStatusCode.BadRequest, "soknadId må være tilsted")
                    }
                }

                route("/soknader") {
                    get {
                        soknadRepository.hentSoknaderForPerson(this.call.authentication.fnr()).let {
                            call.respond(HttpStatusCode.OK, it.map(SoknadResponse.Companion::from))
                        }
                    }
                }

                route("/dokument") {
                    put {
                        dothemagic().also { call.respond(HttpStatusCode.Created) }
                    }

                    post("/{id}") {
                        //    mediator.handle(InnsendingStartetHendelse())
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}

fun dothemagic(): DokumentOpplastningResponse {
    return DokumentOpplastningResponse("19")
}

data class DokumentOpplastningResponse(val id: String)
