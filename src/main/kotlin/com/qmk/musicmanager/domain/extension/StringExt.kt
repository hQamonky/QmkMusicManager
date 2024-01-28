package com.qmk.musicmanager.domain.extension

import com.qmk.musicmanager.domain.model.NamingRule
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
    namingRules.forEach { rule ->
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

fun String.removeParentheses(): String {
    val result = this.replace("\\s*\\([^\\)]*\\)\\s*".toRegex(), "")
    return if (result.contains(")"))
        this.replace("\\(.*\\)".toRegex(), "")
    else result.trim()
}

fun String.removeBrackets(): String {
    val result = this.replace("\\s*\\[[^\\]]*\\]\\s*".toRegex(), "")
    return if (result.contains("]"))
        this.replace("\\[.*\\]".toRegex(), "")
    else result.trim()
}

fun String.removeCurlyBrackets(): String {
    val result = this.replace("\\s*\\{[^\\}]*\\}\\s*".toRegex(), "")
    return if (result.contains("}"))
        this.replace("\\{.*\\}".toRegex(), "")
    else result.trim()
}

fun String.removeAnyKindOfParentheses(): String {
    var result = this
    if (this.contains("(") && this.contains(")")) {
        result = this.removeParentheses()
    }
    if (result.contains("[") && result.contains("]")) {
        result = result.removeBrackets()
    }
    if (result.contains("{") && result.contains("}")) {
        result = result.removeCurlyBrackets()
    }
    return result.trim()
}

fun String.removeLastWord(): String {
    val splitTitle = this.split(" ")
    val lastWord = splitTitle[splitTitle.size-1]
    return this.dropLast(lastWord.length).trim()
}