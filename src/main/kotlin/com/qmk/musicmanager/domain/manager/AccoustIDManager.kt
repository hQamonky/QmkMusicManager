package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.api.AccoustIDAPI
import org.jaudiotagger.audio.AudioFileIO
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


class AccoustIDManager(
    configurationManager: ConfigurationManager = ConfigurationManager(),
    private val api: AccoustIDAPI = AccoustIDAPI(configurationManager),
    private var fpcalc: String? = null
) {
    init {
        if (fpcalc == null) {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            fpcalc = when {
                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    "./tools/linux/fpcalc"
                }

                os.contains("win") -> {
                    "./tools/windows/fpcalc.exe"
                }

                os.contains("mac") -> {
                    "./tools/macos/fpcalc"
                }

                else -> null
            }
        }
    }

    private fun generateAudioFingerprint(file: File): String? {
        if (fpcalc == null) {
            println("Error getting audio fingerprint : fpcalc command not found.")
            return null
        }
        val processBuilder = ProcessBuilder(fpcalc, "-json", file.path)
        return try {
            val process = processBuilder.start()
            val inputStream = process.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append('\n')
            }
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getAudioDuration(file: File): Int {
        val audioFile = AudioFileIO.read(file)
        val audioHeader = audioFile.audioHeader
        return audioHeader.trackLength
    }

    fun lookup(file: File) {
        if (!file.exists()) {
            println("Error getting audio fingerprint : provided file does not exist.")
            return
        }
        val fingerprint = generateAudioFingerprint(file) ?: return
        val duration = getAudioDuration(file)
        api.lookup(duration, fingerprint, listOf("recordings"))
    }
}