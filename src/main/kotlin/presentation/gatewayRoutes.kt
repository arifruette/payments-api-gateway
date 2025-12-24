package presentation

import ServiceConfig
import domain.model.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private fun requireUserId(call: ApplicationCall, bodyUserId: String?): String =
    call.request.headers["X-User-Id"]
        ?: bodyUserId
        ?: call.request.queryParameters["user_id"]
        ?: throw IllegalArgumentException("user_id required (X-User-Id header or user_id query)")

fun Route.gatewayRoutes(
    client: HttpClient,
    services: ServiceConfig,
    correlationIdProvider: (ApplicationCall) -> String
) {
    post("/api/orders") {
        val cid = correlationIdProvider(call)
        val req = call.receive<CreateOrderRequest>()
        val userId = requireUserId(call, req.userId.toString())

        val resp = client.post("${services.orderBaseUrl}/orders") {
            header("X-Correlation-Id", cid)
            header("X-User-Id", userId)
            contentType(ContentType.Application.Json)
            setBody(CreateOrderUpstreamRequest(amount = req.amount, description = req.description))
        }

        val text = resp.bodyAsText()
        if (text.isBlank()) {
            call.respond(HttpStatusCode.Accepted, mapOf("status" to "ACCEPTED", "correlationId" to cid))
        } else {
            call.respondText(text, status = HttpStatusCode.Accepted, contentType = ContentType.Application.Json)
        }
    }

    get("/api/orders") {
        val cid = correlationIdProvider(call)
        val userId = requireUserId(call, bodyUserId = null)

        val limit = call.request.queryParameters["limit"] ?: "50"
        val offset = call.request.queryParameters["offset"] ?: "0"

        val resp = client.get("${services.orderBaseUrl}/orders") {
            header("X-Correlation-Id", cid)
            header("X-User-Id", userId)
            parameter("limit", limit)
            parameter("offset", offset)
        }

        if (resp.status.value == 404) return@get call.respond(HttpStatusCode.NotFound)

        val text = resp.bodyAsText()
        if (text.isBlank()) return@get call.respond(HttpStatusCode.BadGateway)

        call.respondText(text, status = HttpStatusCode.OK, contentType = ContentType.Application.Json)
    }

    get("/api/orders/{id}") {
        val cid = correlationIdProvider(call)
        val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val resp = client.get("${services.orderBaseUrl}/orders/$id") {
            header("X-Correlation-Id", cid)
        }

        if (resp.status.value == 404) return@get call.respond(HttpStatusCode.NotFound)

        val text = resp.bodyAsText()
        if (text.isBlank()) return@get call.respond(HttpStatusCode.BadGateway)

        call.respondText(text, status = HttpStatusCode.OK, contentType = ContentType.Application.Json)
    }

    post("/api/payments/account") {
        val cid = correlationIdProvider(call)
        val req = call.receive<CreateAccountRequest>()
        val userId = requireUserId(call, req.userId.toString())

        val resp = client.post("${services.paymentsBaseUrl}/accounts") {
            header("X-Correlation-Id", cid)
            header("X-User-Id", userId)
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }

        val text = resp.bodyAsText()
        call.respondText(
            text = if (text.isBlank()) "{}" else text,
            status = resp.status,
            contentType = ContentType.Application.Json
        )
    }

    post("/api/payments/topup") {
        val cid = correlationIdProvider(call)
        val req = call.receive<TopUpRequest>()
        val userId = requireUserId(call, req.userId.toString())

        val resp = client.post("${services.paymentsBaseUrl}/accounts/topup") {
            header("X-Correlation-Id", cid)
            header("X-User-Id", userId)
            contentType(ContentType.Application.Json)
            setBody(TopUpUpstreamRequest(amount = req.amount))
        }

        val text = resp.bodyAsText()
        call.respondText(
            text = if (text.isBlank()) "{}" else text,
            status = resp.status,
            contentType = ContentType.Application.Json
        )
    }

    get("/api/payments/balance/{userId}") {
        val cid = correlationIdProvider(call)
        val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val resp = client.get("${services.paymentsBaseUrl}/accounts/balance") {
            header("X-Correlation-Id", cid)
            header("X-User-Id", userId)
        }

        if (resp.status.value == 404) return@get call.respond(HttpStatusCode.NotFound)

        val text = resp.bodyAsText()
        if (text.isBlank()) return@get call.respond(HttpStatusCode.BadGateway)

        call.respondText(text, status = HttpStatusCode.OK, contentType = ContentType.Application.Json)
    }
}