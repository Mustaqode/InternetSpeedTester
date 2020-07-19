package dev.mustaq.internetspeedtest

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by Mustaq Sameer on 18/07/20
 */
class LogInterceptor(private val shouldLog: Boolean) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val requestUrl = request.url
        val responseCode = response.code
        val responseBody = response.body ?: "Response Body is Null"

        val log =
            "$TAG_INTERNET_SPEED_TEST\nRequest: $requestUrl \nResponseCode : $responseCode \nResponseBody: $responseBody"

        if (shouldLog)
            Log.d(TAG_LIBRARY_NAME, log)

        return response
    }

    companion object {
        private const val TAG_LIBRARY_NAME = "InternetSpeedTest"
        private const val TAG_INTERNET_SPEED_TEST = "Speed Tester File Download :"
    }

}