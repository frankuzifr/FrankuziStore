package com.frankuzi.frankuzistore.utils

import android.util.Log

fun myLog(tag: String, message: String?) {
    Log.i(tag, message ?: "")
}

fun myLog(message: String?) {
    myLog("FrankuziStoreLog", message)
}

fun myLog() {
    myLog("VAR")
}