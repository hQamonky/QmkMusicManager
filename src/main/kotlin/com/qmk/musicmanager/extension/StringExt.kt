package com.qmk.musicmanager.extension

import com.qmk.musicmanager.model.NamingRule
import java.io.File
import java.util.concurrent.TimeUnit

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = runCatching {
    ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()

fun String.applyNamingRules(namingRules: List<NamingRule>): String {
    var formattedText = this
    namingRules.forEach {rule ->
        formattedText = formattedText.replace(rule.replace, rule.replaceBy)
    }
    return formattedText
}

fun String.toAuthorizedFileName(): String {
    var formattedText = this
    val forbiddenChars = listOf(
        "/", ":" , "*", "\\", "|", "#", "<", ">", "&", "", "{", "}", "?", "$", "!", "`", "'", "=", "\"", "@", "."
    )
    forbiddenChars.forEach { char ->
        formattedText = formattedText.replace(char, "")
    }
    return formattedText
}