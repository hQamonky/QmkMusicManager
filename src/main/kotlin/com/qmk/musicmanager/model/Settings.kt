package com.qmk.musicmanager.model

import com.google.gson.Gson

data class Settings(
    val downloadOccurrence: Int = 1,
    val musicFolder: String = "~/Music"
) {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}
