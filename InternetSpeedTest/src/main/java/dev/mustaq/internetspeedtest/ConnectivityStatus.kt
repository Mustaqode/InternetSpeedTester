package dev.mustaq.internetspeedtest

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.getSystemService

/**
 * Created by Mustaq Sameer on 18/07/20
 */

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
fun Context.isInternetAvailable(): Boolean? {
    val cm = getSystemService<ConnectivityManager>()
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            getNetworkCapabilities(this.activeNetwork)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            }
        }
    } else {
        return cm?.activeNetworkInfo?.isConnected
    }
}

