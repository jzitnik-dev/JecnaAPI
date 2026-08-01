package io.github.tomhula.jecnaapi

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.KtorNSURLSessionDelegate
import platform.Foundation.*
import platform.darwin.NSObject

class NoRedirectDelegate(
    private val ktorDelegate: KtorNSURLSessionDelegate
) : NSObject(), NSURLSessionDataDelegateProtocol {

    override fun URLSession(session: NSURLSession, dataTask: NSURLSessionDataTask, didReceiveData: NSData) {
        ktorDelegate.URLSession(session, dataTask, didReceiveData)
    }

    override fun URLSession(session: NSURLSession, task: NSURLSessionTask, didCompleteWithError: NSError?) {
        ktorDelegate.URLSession(session, task, didCompleteWithError)
    }

    override fun URLSession(
        session: NSURLSession,
        task: NSURLSessionTask,
        didReceiveChallenge: NSURLAuthenticationChallenge,
        completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit
    ) {
        ktorDelegate.URLSession(session, task, didReceiveChallenge, completionHandler)
    }

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

actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        engine {
            val delegate = KtorNSURLSessionDelegate()

            val customDelegate = NoRedirectDelegate(delegate)
            val sessionConfig = NSURLSessionConfiguration.defaultSessionConfiguration

            val session = NSURLSession.sessionWithConfiguration(sessionConfig, customDelegate, delegateQueue = null)

            usePreconfiguredSession(session, delegate)
        }
        block()
    }
}
