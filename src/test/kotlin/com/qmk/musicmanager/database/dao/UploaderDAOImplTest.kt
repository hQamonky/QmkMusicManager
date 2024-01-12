package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import com.qmk.musicmanager.domain.model.NamingFormat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class UploaderDAOImplTest {

    private lateinit var uploaderDAO: UploaderDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        uploaderDAO = UploaderDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            uploaderDAO.deleteAllUploaders()
        }
    }

    @Test
    fun allUploaders() = runTest {
        uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        uploaderDAO.addNewUploader(
            id = "SwagyTracks",
            name = "SwagyTracks",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        val uploaders = uploaderDAO.allUploaders()
        assert(uploaders.size == 2)
    }

    @Test
    fun uploader() = runTest {
        val uploader = uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        assert(uploader?.id == "UpgradeMusic1")
        val fetchedUploader = uploaderDAO.uploader(uploader!!.id)
        assert(fetchedUploader != null)
        assert(fetchedUploader!!.id == "UpgradeMusic1")
        assert(fetchedUploader.name == "Upgrade")
        assert(fetchedUploader.namingFormat == NamingFormat())
        assert(fetchedUploader.platform == "youtube")
    }

    @Test
    fun addNewUploader() = runTest {
        val uploader = uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        assert(uploader != null)
        assert(uploader!!.id == "UpgradeMusic1")
        assert(uploader.name == "Upgrade")
        assert(uploader.namingFormat == NamingFormat())
        assert(uploader.platform == "youtube")
    }

    @Test
    fun editUploader() = runTest {
        var uploader = uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        assert(uploader?.id == "UpgradeMusic1")
        uploaderDAO.editUploader(
            id = uploader!!.id,
            namingFormat = NamingFormat(
                separator = " / ",
                artistBeforeTitle = false
            )
        )
        uploader = uploaderDAO.uploader(uploader.id)
        assert(uploader?.namingFormat?.separator == " / ")
        assert(uploader?.namingFormat?.artistBeforeTitle == false)
    }

    @Test
    fun deleteUploader() = runTest {
        var uploader = uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        assert(uploader?.id == "UpgradeMusic1")
        uploaderDAO.deleteUploader("UpgradeMusic1")
        uploader = uploaderDAO.uploader("UpgradeMusic1")
        assert(uploader == null)
    }

    @Test
    fun deleteAllUploaders() = runTest {
        uploaderDAO.addNewUploader(
            id = "UpgradeMusic1",
            name = "Upgrade",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        uploaderDAO.addNewUploader(
            id = "SwagyTracks",
            name = "SwagyTracks",
            namingFormat = NamingFormat(),
            platform = "youtube"
        )
        var uploaders = uploaderDAO.allUploaders()
        assert(uploaders.size == 2)
        uploaderDAO.deleteAllUploaders()
        uploaders = uploaderDAO.allUploaders()
        assert(uploaders.isEmpty())
    }
}