package com.qmk.musicmanager.model

data class Settings(
    val version: Double,
    val defaultNamingFormat: NamingFormat,
    val downloadOccurrence: Int
)
