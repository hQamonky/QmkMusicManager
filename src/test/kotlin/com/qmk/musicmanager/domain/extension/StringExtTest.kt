package com.qmk.musicmanager.domain.extension

import org.junit.Test

class StringExtTest {
    @Test
    fun `Test removeParentheses on (ab) ok (cd)`() {
        val text = "(ab) ok (cd)"
        val newText = text.removeParentheses()
        println(newText)
        assert(newText == "ok")
    }

    @Test
    fun `Test removeParentheses on (ab (ok) cd)`() {
        val text = "(ab (ok) cd)"
        val newText = text.removeParentheses()
        println(newText)
        assert(newText == "")
    }

    @Test
    fun `Test removeBrackets on (ab) ok (cd)`() {
        val text = "[ab] ok [cd]"
        val newText = text.removeBrackets()
        println(newText)
        assert(newText == "ok")
    }

    @Test
    fun `Test removeBrackets on (ab (ok) cd)`() {
        val text = "[ab [ok] cd]"
        val newText = text.removeBrackets()
        println(newText)
        assert(newText == "")
    }

    @Test
    fun `Test removeCurlyBrackets on (ab) ok (cd)`() {
        val text = "{ab} ok {cd}"
        val newText = text.removeCurlyBrackets()
        println(newText)
        assert(newText == "ok")
    }

    @Test
    fun `Test removeCurlyBrackets on (ab (ok) cd)`() {
        val text = "{ab {ok} cd}"
        val newText = text.removeCurlyBrackets()
        println(newText)
        assert(newText == "")
    }

    @Test
    fun `Test removeAnyKindOfParentheses on (ab) ok (cd)`() {
        var text = "(ab) ok (cd)".removeAnyKindOfParentheses()
        println(text)
        assert(text == "ok")
        text = "[ab] ok [cd]".removeAnyKindOfParentheses()
        println(text)
        assert(text == "ok")
        text = "{ab} ok {cd}".removeAnyKindOfParentheses()
        println(text)
        assert(text == "ok")
    }

    @Test
    fun `Test removeAnyKindOfParentheses on (ab (ok) cd)`() {
        var text = "(ab (ok) cd)".removeAnyKindOfParentheses()
        println(text)
        assert(text == "")
        text = "[ab [ok] cd]".removeAnyKindOfParentheses()
        println(text)
        assert(text == "")
        text = "{ab {ok} cd}".removeAnyKindOfParentheses()
        println(text)
        assert(text == "")
    }

    @Test
    fun `Test removeLastWord of Hello world!`() {
        val text = "Hello world!".removeLastWord()
        println(text)
        assert(text == "Hello")
    }

    @Test
    fun `Test removeLastWord of Hello world but with more words`() {
        var text = "Hello world but with more words".removeLastWord()
        println(text)
        assert(text == "Hello world but with more")

        text = text.removeLastWord()
        println(text)
        assert(text == "Hello world but with")

        text = text.removeLastWord()
        println(text)
        assert(text == "Hello world but")

        text = text.removeLastWord()
        println(text)
        assert(text == "Hello world")

        text = text.removeLastWord()
        println(text)
        assert(text == "Hello")
    }

    @Test
    fun `Test removeLastWord of space`() {
        val text = " ".removeLastWord()
        println(text)
        assert(text == "")
    }

    @Test
    fun `Test removeLastWord of nothing`() {
        val text = "".removeLastWord()
        println(text)
        assert(text == "")
    }
}