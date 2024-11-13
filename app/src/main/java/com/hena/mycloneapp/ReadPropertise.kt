package com.hena.mycloneapp

import android.content.Context
import java.util.Properties

fun loadApiKey(context: Context): String? {
    val properties = Properties()
    try {

        context.assets.open("keys.properties").use { inputStream ->
            properties.load(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    // API_KEY 가져오기
    return properties.getProperty("MICRODUST_KEY")
}
