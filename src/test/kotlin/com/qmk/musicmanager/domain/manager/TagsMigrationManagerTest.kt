package com.qmk.musicmanager.domain.manager

import com.qmk.musicmanager.database.dao.*
import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.Settings
import kotlinx.coroutines.test.*
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.Tag
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class TagsMigrationManagerTest {

    private val manager = TagsMigrationManager()
    private lateinit var configurationManager: ConfigurationManager

    @Before
    fun setUp() {

        configurationManager = ConfigurationManager()
        configurationManager.setConfiguration(
            Settings(
                audioFolder = "src/test/DataMigrationTest/Audio",
                playlistsFolder = "src/test/DataMigrationTest/Playlists",
                archiveFolder = "src/test/DataMigrationTest/Archive"
            )
        )
    }

    @After
    fun tearDown() {
        configurationManager.setConfiguration(Settings())
    }

    @Test
    fun getAudioFiles() {
        val files = manager.getAudioFiles()
        assert(files?.size == 1545)
    }

    @Test
    fun isMetadataIsOld() {
        val files = manager.getAudioFiles()
        assert(files != null)
        val areFilesOldMetadata = files!!.map {
            val f = AudioFileIO.read(it)
            val tag: Tag = f.tag
            manager.isMetadataIsOld(tag)
        }
        val numberOfOldFiles = areFilesOldMetadata.mapNotNull {
            if (it) {
                "old metadata"
            } else {
                null
            }
        }.size
        assert(numberOfOldFiles == 1545)
    }

    @Test
    fun getNewCommentTagFromOldFile() {
        val mopidyManager = MopidyManager()
        val files = manager.getAudioFiles()
        assert(files != null)
        val casual = files!!.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Casual")) {
                it.nameWithoutExtension
            } else null
        }
        var playlist = mopidyManager.getFilesFromPlaylist("Casual")
        assert(playlist.isNotEmpty())
        var buggedFiles = mutableListOf<String>()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        var duplicates = mutableListOf<String>()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(casual.size == playlist.size)
        val chill = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Chill")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Chill")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(chill.size == playlist.size)
        val deppHouseMix = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Deep House Mix")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Deep House Mix")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(deppHouseMix.size == playlist.size)
        val mix = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Mix")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Mix")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(mix.size == playlist.size)
        val partyHard = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Party Hard")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Party Hard")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(partyHard.size == playlist.size)
        val reflectOnLife = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Reflect on Life")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Reflect on Life")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(reflectOnLife.size == playlist.size)
        val relax = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Relax")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Relax")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(relax.size == playlist.size)
        val sunshineRose = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Sunshine Rosé")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Sunshine Rosé")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(sunshineRose.size == playlist.size)
        val vicGazole = files.mapNotNull {
            val tag = manager.getNewCommentTagFromOldFile(it)
            if (tag.playlists.contains("Vic Gazole")) {
                it.nameWithoutExtension
            } else null
        }
        playlist = mopidyManager.getFilesFromPlaylist("Vic Gazole")
        assert(playlist.isNotEmpty())
        buggedFiles = mutableListOf()
        playlist.forEach { music ->
            if (!files.map { it.name }.contains(File(music).name))
                buggedFiles.add(music)
        }
        assert(buggedFiles.size == 0)
        duplicates = mutableListOf()
        playlist.forEach { it1 ->
            val file1 = File(it1)
            var count = 0
            playlist.forEach { it2 ->
                val file2 = File(it2)
                if (file1.name == file2.name) {
                    count++
                }
            }
            if (count > 1) {
                duplicates.add(file1.name)
            }
        }
        assert(duplicates.size == 0)
        assert(vicGazole.size == playlist.size)
    }

    @Test
    fun convertAllFilesMetadata() = runTest {
            configurationManager.setAudioFolder("src/test/DataMigrationTest/TestAudio")
            DatabaseFactory.init()
            val playlistDAO = PlaylistDAOImpl()
            val platformPlaylistDAO = PlatformPlaylistDAOImpl()
            val musicDAO = MusicDAOImpl()
            val namingRuleDAO = NamingRuleDAOImpl()
            val uploaderDAO = UploaderDAOImpl()
            val tagDAO = TagDAOImpl()
            val mopidyManager = MopidyManager()
            val powerAmpManager = PowerAmpManager()
            val dataManager = DataManager(
                configurationManager,
                playlistDAO,
                platformPlaylistDAO,
                musicDAO,
                namingRuleDAO,
                uploaderDAO,
                tagDAO,
                mopidyManager,
                powerAmpManager
            )

            val casual = mopidyManager.getFilesFromPlaylist("Casual")
            val chill = mopidyManager.getFilesFromPlaylist("Chill")
            val deepHouseMix = mopidyManager.getFilesFromPlaylist("Deep House Mix")
            val mix = mopidyManager.getFilesFromPlaylist("Mix")
            val partyHard = mopidyManager.getFilesFromPlaylist("Party Hard")
            val reflectOnLife = mopidyManager.getFilesFromPlaylist("Reflect on Life")
            val relax = mopidyManager.getFilesFromPlaylist("Relax")
            val sunshineRose = mopidyManager.getFilesFromPlaylist("Sunshine Rosé")
            val vicGazole = mopidyManager.getFilesFromPlaylist("Vic Gazole")

            assert(casual.isNotEmpty())
            assert(chill.isNotEmpty())
            assert(deepHouseMix.isNotEmpty())
            assert(mix.isNotEmpty())
            assert(partyHard.isNotEmpty())
            assert(reflectOnLife.isNotEmpty())
            assert(relax.isNotEmpty())
            assert(sunshineRose.isNotEmpty())
            assert(vicGazole.isNotEmpty())

            manager.convertAllFilesMetadata()
            configurationManager.setPlaylistsFolder("src/test/DataMigrationTest/TestPlaylists")
            dataManager.addFilesToDatabase()

            val newCasual = mopidyManager.getFilesFromPlaylist("Casual")
            val newChill = mopidyManager.getFilesFromPlaylist("Chill")
            val newDeepHouseMix = mopidyManager.getFilesFromPlaylist("Deep House Mix")
            val newMix = mopidyManager.getFilesFromPlaylist("Mix")
            val newPartyHard = mopidyManager.getFilesFromPlaylist("Party Hard")
            val newReflectOnLife = mopidyManager.getFilesFromPlaylist("Reflect on Life")
            val newRelax = mopidyManager.getFilesFromPlaylist("Relax")
            val newSunshineRose = mopidyManager.getFilesFromPlaylist("Sunshine Rosé")
            val newVicGazole = mopidyManager.getFilesFromPlaylist("Vic Gazole")

            assert(casual.size == newCasual.size)
            assert(chill.size == newChill.size)
            assert(deepHouseMix.size == newDeepHouseMix.size)
            assert(mix.size == newMix.size)
            assert(partyHard.size == newPartyHard.size)
            assert(reflectOnLife.size == newReflectOnLife.size)
            assert(relax.size == newRelax.size)
            assert(sunshineRose.size == newSunshineRose.size)
            assert(vicGazole.size == newVicGazole.size)
        }
}