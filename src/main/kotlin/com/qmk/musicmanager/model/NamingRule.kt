package com.qmk.musicmanager.model

data class NamingRule(
    val id: Int,
    val replace: String,
    val replaceBy: String,
    val priority: Int
    )