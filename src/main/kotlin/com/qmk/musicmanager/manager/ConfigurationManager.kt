package com.qmk.musicmanager.manager

import com.google.gson.Gson
import com.qmk.musicmanager.model.Settings
import java.io.File

class ConfigurationManager(private val configurationFile: File = File("./src/main/resources/configuration/configuration.json")) {
    fun getConfiguration(): Settings {
        return Gson().fromJson(configurationFile.readText(Charsets.UTF_8), Settings::class.java)
    }

    fun setConfiguration(settings: Settings) {
        configurationFile.writeText(Gson().toJson(settings))
    }
}