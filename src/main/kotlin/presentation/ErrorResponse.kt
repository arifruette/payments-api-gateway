package presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.application.install
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val code: String, val message: String)

fun Application.installErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, e ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("BAD_REQUEST", e.message ?: "bad request"))
        }
        exception<Throwable> { call, e ->
            call.respond(HttpStatusCode.BadGateway, ErrorResponse("UPSTREAM_ERROR", e.message ?: "upstream error"))
        }
    }
}
