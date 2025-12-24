package domain.usecase

import domain.model.CreateAccountRequest
import domain.model.TopUpRequest
import domain.model.requireUuid
import domain.ports.PaymentsApi


class CreateAccountUseCase(private val api: PaymentsApi) {
    suspend fun execute(req: CreateAccountRequest, correlationId: String) =
        api.createAccount(req.copy(userId = requireUuid(req.userId.toString())), correlationId)
}

class TopUpUseCase(private val api: PaymentsApi) {
    suspend fun execute(req: TopUpRequest, correlationId: String) =
        api.topUp(req.copy(userId = requireUuid(req.userId.toString())), correlationId)
}

class GetBalanceUseCase(private val api: PaymentsApi) {
    suspend fun execute(userId: String, correlationId: String) =
        api.getBalance(requireUuid(userId).toString(), correlationId)
}
