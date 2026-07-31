package io.github.tomhula.jecnaapi

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.KtorNSURLSessionDelegate
import platform.Foundation.*
import platform.darwin.NSObject

// 1. Create a custom delegate to intercept the redirect
class NoRedirectDelegate(
    private val ktorDelegate: KtorNSURLSessionDelegate
) : NSObject(), NSURLSessionDataDelegateProtocol {

    override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
        ktorDelegate.URLSession(session, dataTask, didReceiveData)
    }

    override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
        ktorDelegate.URLSession(session, task, didCompleteWithError)
    }

    // 2. Catch the redirect and kill it by passing null
    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        willPerformHTTPRedirection: NSHTTPURLResponse,
        newRequest: NSURLRequest,
        completionHandler: (NSURLRequest?) -> Unit
    ) {
        completionHandler(null)
    }
}

// 3. Inject it into Ktor
actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        engine {
            val delegate = KtorNSURLSessionDelegate()
            val customDelegate = NoRedirectDelegate(delegate)
            val sessionConfig = NSURLSessionConfiguration.defaultSessionConfiguration

            // Apply the custom delegate to the session
            val session = NSURLSession.sessionWithConfiguration(sessionConfig, customDelegate, delegateQueue = null)
            usePreconfiguredSession(session, delegate)
        }
        block()
    }
}
