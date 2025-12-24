package domain.usecase

import domain.model.CreateOrderRequest
import domain.model.requireUuid
import domain.ports.OrderApi


class CreateOrderUseCase(private val api: OrderApi) {
    suspend fun execute(req: CreateOrderRequest, correlationId: String) =
        api.createOrder(
            req.copy(userId = requireUuid(req.userId.toString())), correlationId,
            userId = req.userId.toString()
        )
}

class GetOrderUseCase(private val api: OrderApi) {
    suspend fun execute(id: String, correlationId: String) =
        api.getOrder(requireUuid(id).toString(), correlationId)
}
