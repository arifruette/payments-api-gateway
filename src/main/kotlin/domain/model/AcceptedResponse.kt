package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AcceptedResponse(
    val status: String = "ACCEPTED",
    val correlationId: String
)
