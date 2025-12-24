import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import presentation.gatewayRoutes
import java.net.ConnectException
import java.util.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

data class ServiceConfig(
    val orderBaseUrl: String,
    val paymentsBaseUrl: String
)

private fun correlationId(): String = UUID.randomUUID().toString()

fun Application.module() {
    val config = environment.config

    val services = ServiceConfig(
        orderBaseUrl = config.property("services.order.baseUrl").getString(),
        paymentsBaseUrl = config.property("services.payments.baseUrl").getString()
    )

    val client = HttpClient(CIO) {
        install(ClientContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
    }

    install(CallLogging)

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
        )
    }

    install(StatusPages) {
        exception<ConnectException> { call, cause ->
            call.application.environment.log.error("Downstream service unavailable", cause)
            call.respond(
                status = HttpStatusCode.BadGateway,
                message = mapOf(
                    "error" to "Service unavailable",
                    "details" to (cause.message ?: "Cannot connect to downstream service")
                )
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unexpected error in gateway", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = mapOf(
                    "error" to "Gateway error",
                    "details" to (cause.message ?: "Unexpected error")
                )
            )
        }
    }

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi.yaml")

        gatewayRoutes(
            client = client,
            services = services,
            correlationIdProvider = { call ->
                call.request.headers["X-Correlation-Id"] ?: correlationId()
            }
        )

        get("/") {
            call.respondRedirect("/swagger")
        }

        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }
    }
}
