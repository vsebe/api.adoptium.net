package net.adoptopenjdk.api

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.adoptopenjdk.api.v3.JsonMapper
import net.adoptopenjdk.api.v3.TimeSource
import net.adoptopenjdk.api.v3.dataSources.APIDataStore
import net.adoptopenjdk.api.v3.dataSources.APIDataStoreImpl
import net.adoptopenjdk.api.v3.dataSources.UpdaterJsonMapper
import net.adoptopenjdk.api.v3.dataSources.persitence.ApiPersistence
import net.adoptopenjdk.api.v3.models.Binary
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class APIDataStoreTest : MongoTest() {

    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(this::class.java)
    }

    @Test
    fun reposHasElements() {
        runBlocking {
            val repo = BaseTest.adoptRepos
            assert(repo.getFeatureRelease(8)!!.releases.getReleases().toList().size > 0)
        }
    }

    @Test
    fun `can deserialize binaries`() {
        val binary = BaseTest.adoptRepos.allReleases
            .getReleases()
            .first()
            .binaries
            .first()
        val str = JsonMapper.mapper.writeValueAsString(binary)

        val read = JsonMapper.mapper.readValue(str, Binary::class.java)
        assertEquals(binary, read)
    }

    @Test
    fun dataIsStoredToDbCorrectly(apiDataStore: APIDataStore, apiPersistence: ApiPersistence) {
        runBlocking {
            apiPersistence.updateAllRepos(BaseTest.adoptRepos, "")
            val dbData = apiDataStore.loadDataFromDb(false)

            val before = UpdaterJsonMapper.mapper.writeValueAsString(dbData)
            val after = UpdaterJsonMapper.mapper.writeValueAsString(BaseTest.adoptRepos)
            JSONAssert.assertEquals(
                before,
                after,
                true
            )
        }
    }

    @Test
    fun `updated at is set`(apiPersistence: ApiPersistence) {
        runBlocking {
            apiPersistence.updateAllRepos(BaseTest.adoptRepos, "")
            val time = TimeSource.now()
            delay(1000)
            apiPersistence.updateAllRepos(BaseTest.adoptRepos, "a-checksum")

            val updatedTime = apiPersistence.getUpdatedAt()

            assertTrue(updatedTime.time.isAfter(time))
            assertEquals("a-checksum", updatedTime.checksum)
        }
    }

    @Test
    fun `update is not scheduled by default`(apiDataStore: APIDataStore) {
        assertNull((apiDataStore as APIDataStoreImpl).schedule)
    }
}
