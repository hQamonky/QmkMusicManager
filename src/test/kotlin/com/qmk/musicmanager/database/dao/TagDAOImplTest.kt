package com.qmk.musicmanager.database.dao

import com.qmk.musicmanager.database.model.DatabaseFactory
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

import org.junit.Before

class TagDAOImplTest {

    private lateinit var tagDAO: TagDAO
    private lateinit var musicDAO: MusicDAO

    @Before
    fun setUp() {
        DatabaseFactory.init()
        tagDAO = TagDAOImpl()
        musicDAO = MusicDAOImpl()
    }

    @After
    fun tearDown() {
        runBlocking {
            tagDAO.deleteAllTags()
        }
    }

    @Test
    fun allTags() = runTest {
        musicDAO.addTagToMusic("Rap", "J-Wright - Winter's Over (Prod. Beatcraze)")
        musicDAO.addTagToMusic("Chill", "J-Wright - Winter's Over (Prod. Beatcraze)")
        musicDAO.addTagToMusic("Chill", "Retromigration - BO")

        val tags = tagDAO.allTags()
        assert(tags.size == 2)
    }

    @Test
    fun renameTag() = runTest {
        musicDAO.addTagToMusic("Rap", "J-Wright - Winter's Over (Prod. Beatcraze)")

        var tags = tagDAO.allTags()
        assert(tags.size == 1)
        assert(tags[0] == "Rap")

        tagDAO.renameTag("Rap", "Chill")

        tags = tagDAO.allTags()
        assert(tags.size == 1)
        assert(tags[0] == "Chill")
    }

    @Test
    fun deleteTag() = runTest {
        musicDAO.addTagToMusic("Rap", "J-Wright - Winter's Over (Prod. Beatcraze)")

        var tags = tagDAO.allTags()
        assert(tags.size == 1)
        assert(tags[0] == "Rap")

        tagDAO.deleteTag("Rap")

        tags = tagDAO.allTags()
        assert(tags.isEmpty())
    }

    @Test
    fun deleteAllTags() = runTest {
        musicDAO.addTagToMusic("Rap", "J-Wright - Winter's Over (Prod. Beatcraze)")
        musicDAO.addTagToMusic("Chill", "J-Wright - Winter's Over (Prod. Beatcraze)")

        var tags = tagDAO.allTags()
        assert(tags.size == 2)

        tagDAO.deleteAllTags()

        tags = tagDAO.allTags()
        assert(tags.isEmpty())
    }
}