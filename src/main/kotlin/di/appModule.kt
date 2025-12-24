package di

import domain.ports.OrderApi
import domain.ports.PaymentsApi
import domain.usecase.*
import infra.config.AppConfig
import infra.http.KtorPaymentsApi
import infra.http.OrderApiHttp
import infra.http.httpClient
import io.ktor.server.config.*
import org.koin.dsl.module

fun appModule(cfg: ApplicationConfig) = module {
    single {
        AppConfig(
            upstream = AppConfig.Upstream(
                orderBaseUrl = cfg.property("app.upstream.orderBaseUrl").getString(),
                paymentsBaseUrl = cfg.property("app.upstream.paymentsBaseUrl").getString()
            ),
            http = AppConfig.Http(
                connectTimeoutMs = cfg.property("app.http.connectTimeoutMs").getString().toLong(),
                requestTimeoutMs = cfg.property("app.http.requestTimeoutMs").getString().toLong()
            )
        )
    }

    single { httpClient(get<AppConfig>().http.connectTimeoutMs, get<AppConfig>().http.requestTimeoutMs) }

    single<OrderApi> { OrderApiHttp(get(), get<AppConfig>().upstream.orderBaseUrl) }
    single<PaymentsApi> { KtorPaymentsApi(get(), get<AppConfig>().upstream.paymentsBaseUrl) }

    single { CreateOrderUseCase(get()) }
    single { GetOrderUseCase(get()) }
    single { CreateAccountUseCase(get()) }
    single { TopUpUseCase(get()) }
    single { GetBalanceUseCase(get()) }
}
