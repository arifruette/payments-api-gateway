package domain.ports

import domain.model.AcceptedResponse
import domain.model.CreateOrderRequest

interface OrderApi {
    suspend fun createOrder(req: CreateOrderRequest, correlationId: String, userId: String): AcceptedResponse
    suspend fun getOrder(id: String, correlationId: String): String
}
