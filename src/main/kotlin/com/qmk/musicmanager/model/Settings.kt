package com.qmk.musicmanager.model

import com.google.gson.Gson

data class Settings(
    val downloadOccurrence: Int = 1,
    val musicFolder: String = "./Music",
    val autoDownload: Boolean = true
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}
