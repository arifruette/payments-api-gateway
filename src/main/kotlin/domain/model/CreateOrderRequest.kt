package domain.model

import infra.serialization.UuidSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateOrderRequest(
    @Serializable(with = UuidSerializer::class)
    val userId: UUID,
    val amount: Long,
    val description: String
)

@Serializable
data class CreateOrderUpstreamRequest(
    val amount: Long,
    val description: String
)

@Serializable
data class CreateAccountRequest(
    @Serializable(with = UuidSerializer::class)
    val userId: UUID
)

@Serializable
data class TopUpRequest(
    @Serializable(with = UuidSerializer::class)
    val userId: UUID,
    val amount: Long
)

@Serializable
data class TopUpUpstreamRequest(
    val amount: Long
)

@Serializable
data class OrderDto(
    val id: String,
    @Serializable(with = UuidSerializer::class)
    val userId: UUID,
    val amount: Long,
    val description: String,
    val status: String
)

@Serializable
data class AccountDto(
    val userId: String
)

@Serializable
data class BalanceDto(
    @Serializable(with = UuidSerializer::class)
    val userId: UUID,
    val balance: Long
)

fun requireUuid(s: String): UUID = UUID.fromString(s)
