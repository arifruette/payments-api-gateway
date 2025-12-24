package infra.http

import domain.model.AcceptedResponse
import domain.model.CreateOrderRequest
import domain.model.CreateOrderUpstreamRequest
import domain.ports.OrderApi
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType

class OrderApiHttp(
    private val client: HttpClient,
    private val baseUrl: String
) : OrderApi {

    override suspend fun createOrder(req: CreateOrderRequest, correlationId: String, userId: String): AcceptedResponse {
        client.post("$baseUrl/orders") {
            header("X-Correlation-Id", correlationId)
            header("X-User-Id", userId)
            contentType(ContentType.Application.Json)
            setBody(CreateOrderUpstreamRequest(amount = req.amount, description = req.description))
        }
        return AcceptedResponse(correlationId = correlationId)
    }

    override suspend fun getOrder(id: String, correlationId: String): String {
        val resp = client.get("$baseUrl/orders/$id") {
            header("X-Correlation-Id", correlationId)
        }
        return resp.bodyAsText()
    }
}
