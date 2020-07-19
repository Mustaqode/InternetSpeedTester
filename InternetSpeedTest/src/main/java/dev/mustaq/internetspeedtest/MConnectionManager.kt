package dev.mustaq.internetspeedtest

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Created by Mustaq Sameer on 18/07/20
 */
class MConnectionManager {

    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var testDownloadUrl: String = URL_TEST
    private var suspend: Boolean = false
    private var log: Boolean = false

    private val context by lazy {
        activity ?: fragment?.requireActivity()
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LogInterceptor(log))
            .build()
    }

    private val request: Request by lazy {
        Request.Builder()
            .url(testDownloadUrl)
            .build()
    }

    fun checkInternetAvailability(): Boolean =
        context?.isInternetAvailable() ?: false

    fun getInternetSpeed(runOnUiThread: Boolean = false, onResponse: (InternetSpeed) -> Unit) {
        if (checkInternetAvailability()) {
            if (suspend)
                checkInternetSpeedAsync(runOnUiThread, onResponse)
            else checkInternetSpeed(runOnUiThread, onResponse)
        } else {
            logError(ERROR_NO_INTERNET)
            onResponse.invoke(InternetSpeed.UNKNOWN)
        }
    }


    private fun checkInternetSpeedAsync(
        runOnUiThread: Boolean,
        onResponse: (InternetSpeed) -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        GlobalScope.launch {
            val internetSpeed = withContext(Dispatchers.IO) {
                suspendCancellableCoroutine<InternetSpeed> { continuation ->
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            logError(e.localizedMessage)
                            continuation.resumeWithException(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                logSuccess(response.code.toString())
                                val endTime = System.currentTimeMillis()
                                continuation.resume(
                                    checkInternetSpeed(
                                        startTime,
                                        endTime
                                    )
                                )
                            }
                        }
                    })
                }
            }
            if (runOnUiThread) {
                context?.runOnUiThread {
                    onResponse.invoke(internetSpeed)
                }
            } else onResponse.invoke(internetSpeed)
        }
    }

    private fun checkInternetSpeed(
        runOnUiThread: Boolean,
        onResponse: ((InternetSpeed) -> Unit)? = null
    ) {
        val startTime = System.currentTimeMillis()
        var internetSpeed: InternetSpeed? = null

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logError(e.localizedMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    logSuccess(response.code.toString())
                    internetSpeed = checkInternetSpeed(startTime, System.currentTimeMillis())
                    if (runOnUiThread) {
                        context?.runOnUiThread {
                            onResponse?.invoke(internetSpeed!!)
                        }
                    }
                }
            }
        })
    }

    private fun checkInternetSpeed(startTime: Long, endTime: Long): InternetSpeed {
        val responseTimeInMilli = endTime - startTime
        val responseTimeInSec = TimeUnit.MILLISECONDS.toSeconds(responseTimeInMilli)
        val kilobytePerSecond = ((1024.0 / responseTimeInSec)).toInt()
        val internetSpeed = InternetSpeed.getSpeed(kilobytePerSecond)
        logSpeed(internetSpeed)

        return internetSpeed
    }

//    private fun checkDownloadSpeed(startTime: Long, endTime: Long, response: Response): Long {
//        val responseTimeInMilli = endTime - startTime
//       return response.body?.byteStream().use { input ->
//            val bos = ByteArrayOutputStream()
//            val buffer = ByteArray(1024)
//            while (input?.read(buffer) != -1) {
//                bos.write(buffer)
//            }
//            TimeUnit.MILLISECONDS.toSeconds(bos.toByteArray().size / responseTimeInMilli)
//        }
//    }

    @SuppressLint("LogNotTimber")
    private fun logSpeed(internetSpeed: InternetSpeed) {
        if (log) {
            Log.d(TAG_INTERNET_SPEED, internetSpeed.toString())
        }
    }

    @SuppressLint("LogNotTimber")
    private fun logError(message: String?) {
        if (log) {
            Log.d(TAG_INTERNET_SPEED, message ?: "Error")
        }
    }

    @SuppressLint("LogNotTimber")
    private fun logSuccess(message: String?) {
        if (log) {
            Log.d(TAG_INTERNET_SPEED, message ?: "Success")
        }
    }


    class Builder {

        private val mConnectionManager = MConnectionManager()

        fun context(fragment: Fragment): Builder {
            mConnectionManager.fragment = fragment
            return this
        }

        fun context(activity: Activity): Builder {
            mConnectionManager.activity = activity
            return this
        }

        fun suspend(suspend: Boolean): Builder {
            mConnectionManager.suspend = suspend
            return this
        }

        fun log(log: Boolean): Builder {
            mConnectionManager.log = log
            return this
        }

        fun setUrl(url: String): Builder {
            mConnectionManager.testDownloadUrl = url
            return this
        }

        fun build(): MConnectionManager = mConnectionManager

    }

    companion object {
        private const val ERROR_NO_INTERNET = "You have no active internet connection"
        private const val TAG_INTERNET_SPEED = "Internet Speed:"
        private const val URL_TEST = "https://google.com"
    }

}