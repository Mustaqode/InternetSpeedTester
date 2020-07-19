package dev.mustaq.internetspeedtest

/**
 * Created by Mustaq Sameer on 18/07/20
 */
enum class InternetSpeed(val speed: Int) {
    POOR(150),
    AVERAGE(550),
    GOOD(2000),
    UNKNOWN(0);

    companion object {
        fun getSpeed(speedInKb: Int): InternetSpeed =
            values().find { speedInKb < it.speed } ?: GOOD
    }
}