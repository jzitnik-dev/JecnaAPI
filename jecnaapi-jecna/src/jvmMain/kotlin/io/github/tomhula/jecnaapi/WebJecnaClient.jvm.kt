package io.github.tomhula.jecnaapi

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient {
        block()
    }
}
