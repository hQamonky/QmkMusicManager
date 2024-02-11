package com.qmk.musicmanager.extension

import com.google.gson.Gson

fun <T> Any.fromJson(classOfT: Class<T>?): T {
    val gson = Gson()
    val json = gson.toJson(this)
    return gson.fromJson(json, classOfT)
}