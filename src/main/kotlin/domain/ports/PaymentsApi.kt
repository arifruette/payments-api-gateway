package domain.ports

import domain.model.AccountDto
import domain.model.BalanceDto
import domain.model.CreateAccountRequest
import domain.model.TopUpRequest


interface PaymentsApi {
    suspend fun createAccount(req: CreateAccountRequest, correlationId: String): AccountDto
    suspend fun topUp(req: TopUpRequest, correlationId: String): BalanceDto
    suspend fun getBalance(userId: String, correlationId: String): BalanceDto?
}
