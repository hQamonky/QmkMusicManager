package com.qmk.musicmanager.model

data class NamingRule(
    val id: String? = null,
    val replace: String,
    val replaceBy: String = "",
    val priority: Int = 2
    )