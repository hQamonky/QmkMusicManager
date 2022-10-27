package com.qmk.musicmanager.model

data class Uploader(
    val id: String,
    val name: String,
    val namingFormat: NamingFormat = NamingFormat()
)
