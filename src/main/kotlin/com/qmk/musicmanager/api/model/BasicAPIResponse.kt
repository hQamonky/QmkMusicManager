package com.qmk.musicmanager.api.model

data class BasicAPIResponse(
    val successful: Boolean,
    val message: String? = null
)
