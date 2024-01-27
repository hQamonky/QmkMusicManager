package com.qmk.musicmanager.domain.manager

import com.google.gson.Gson
import com.qmk.musicmanager.api.AccoustIDAPI
import com.qmk.musicmanager.api.MusicBrainzAPI
import com.qmk.musicmanager.api.MusicBrainzAPI.LookupRequest
import okhttp3.Response
import org.jaudiotagger.audio.AudioFileIO
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


class AccoustIDManager(
    configurationManager: ConfigurationManager = ConfigurationManager(),
    private val api: AccoustIDAPI = AccoustIDAPI(configurationManager),
    private val musicBrainzApi: MusicBrainzAPI = MusicBrainzAPI(),
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

//    private fun getAudioDuration(file: File): Int {
//        val audioFile = AudioFileIO.read(file)
//        val audioHeader = audioFile.audioHeader
//        return audioHeader.trackLength
//    }

    suspend fun searchInfo(file: File): String? {
        if (!file.exists()) {
            println("Error getting audio fingerprint : provided file does not exist.")
            return null
        }
        val fingerprint = generateAudioFingerprint(file) ?: return null
        val audioData = Gson().fromJson(fingerprint, FingerPrint::class.java)
//        val duration = getAudioDuration(file)
        if (audioData.duration > 900) return null
        val response = api.lookupRecordingIds(audioData.duration, audioData.fingerprint)
        if (response.isSuccessful) {
            val result = Gson().fromJson(response.message, AccoustIDAPI.LookupRecordingIdsResponse::class.java)
            val mBResult = getInfoFromMB(result.results[0].recordings[0].id)
            if (!mBResult.isSuccessful) {
                println("MusicBrainz error : ${mBResult.message}")
                return null
            }
            return mBResult.message
        } else {
            val result = Gson().fromJson(response.message, AccoustIDAPI.ErrorResponse::class.java)
            println(result.error.message)
            return null
        }
    }

    private suspend fun getInfoFromMB(recordingId: String): Response {
        return musicBrainzApi.lookup(
            LookupRequest.EntityType.RECORDING,
            recordingId,
            listOf("recording", "artist", "genre")
        )
    }

    data class FingerPrint(
        val fingerprint: String,
        val duration: Double
    )
}