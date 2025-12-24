package infra.config

data class AppConfig(
    val upstream: Upstream,
    val http: Http
) {
    data class Upstream(
        val orderBaseUrl: String,
        val paymentsBaseUrl: String
    )
    data class Http(
        val connectTimeoutMs: Long,
        val requestTimeoutMs: Long
    )
}
