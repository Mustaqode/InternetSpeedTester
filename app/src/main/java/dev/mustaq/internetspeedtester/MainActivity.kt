package dev.mustaq.internetspeedtester

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import dev.mustaq.internetspeedtest.InternetSpeed
import dev.mustaq.internetspeedtest.MConnectionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getInternetSpeed()
    }

    private fun getInternetSpeed() {
        val connectionManager = MConnectionManager.Builder()
            .context(this)
            .log(true)
            .suspend(true)
            .build()

        connectionManager.getInternetSpeed(true) {
            showToast(it)
        }
    }

    private fun showToast(intenetSpeed: InternetSpeed) {
        Toast.makeText(this, intenetSpeed.toString(), Toast.LENGTH_LONG).show()

    }
}