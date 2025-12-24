package infra.http

import domain.model.AccountDto
import domain.model.BalanceDto
import domain.model.CreateAccountRequest
import domain.model.TopUpRequest
import domain.ports.PaymentsApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class KtorPaymentsApi(
    private val client: HttpClient,
    private val baseUrl: String
) : PaymentsApi {

    override suspend fun createAccount(req: CreateAccountRequest, correlationId: String): AccountDto {
        val resp = client.post("$baseUrl/accounts") {
            header("X-Correlation-Id", correlationId)
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (!resp.status.isSuccess()) error("UPSTREAM_PAYMENTS_${resp.status.value}")
        return resp.body()
    }

    override suspend fun topUp(req: TopUpRequest, correlationId: String): BalanceDto {
        val resp = client.post("$baseUrl/accounts/topup") {
            header("X-Correlation-Id", correlationId)
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (!resp.status.isSuccess()) error("UPSTREAM_PAYMENTS_${resp.status.value}")
        return resp.body()
    }

    override suspend fun getBalance(userId: String, correlationId: String): BalanceDto? {
        val resp = client.get("$baseUrl/accounts/$userId/balance") {
            header("X-Correlation-Id", correlationId)
        }
        return when {
            resp.status.value == 404 -> null
            resp.status.isSuccess() -> resp.body()
            else -> error("UPSTREAM_PAYMENTS_${resp.status.value}")
        }
    }
}
