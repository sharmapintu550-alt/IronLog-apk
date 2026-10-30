package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.WeeklySplitEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WeeklyScheduleSyncTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserScopedWeeklySplitsIsolation() = runBlocking {
        val dao = db.weeklySplitDao()

        // 1. Insert 7 days for guest
        val guestSplits = (1..7).map { day ->
            WeeklySplitEntity(
                dayOfWeek = day,
                routineId = if (day == 1) "guest_routine_legs" else null,
                routineTitle = if (day == 1) "Guest Legs" else null,
                isRestDay = day != 1,
                userId = "guest"
            )
        }
        dao.insertAll(guestSplits)

        // 2. Insert 7 days for authenticated user "user_cloud_99"
        val userSplits = (1..7).map { day ->
            WeeklySplitEntity(
                dayOfWeek = day,
                routineId = if (day == 1) "cloud_routine_chest" else if (day == 3) "cloud_routine_back" else null,
                routineTitle = if (day == 1) "Cloud Heavy Chest" else if (day == 3) "Cloud Back Day" else null,
                isRestDay = (day != 1 && day != 3),
                userId = "user_cloud_99"
            )
        }
        dao.insertAll(userSplits)

        // 3. Verify guest query gets exactly guest splits
        val retrievedGuest = dao.getSplitsForUser("guest").first()
        assertEquals(7, retrievedGuest.size)
        assertEquals("guest_routine_legs", retrievedGuest[0].routineId)
        assertEquals("Guest Legs", retrievedGuest[0].routineTitle)
        assertEquals("Guest Legs", retrievedGuest[0].routineName)
        assertFalse(retrievedGuest[0].isRestDay)
        assertTrue(retrievedGuest[1].isRestDay)

        // 4. Verify user_cloud_99 gets exactly cloud user splits without data leak
        val retrievedUser = dao.getSplitsForUser("user_cloud_99").first()
        assertEquals(7, retrievedUser.size)
        assertEquals("cloud_routine_chest", retrievedUser[0].routineId)
        assertEquals("Cloud Heavy Chest", retrievedUser[0].routineTitle)
        assertEquals("Cloud Heavy Chest", retrievedUser[0].routineName)
        assertFalse(retrievedUser[0].isRestDay)

        assertEquals("cloud_routine_back", retrievedUser[2].routineId)
        assertEquals("Cloud Back Day", retrievedUser[2].routineTitle)
        assertFalse(retrievedUser[2].isRestDay)

        // Day 2 should be rest day
        assertTrue(retrievedUser[1].isRestDay)

        // 5. Test single day query
        val day1User = dao.getSplitForDayAndUserSync(1, "user_cloud_99")
        assertNotNull(day1User)
        assertEquals("cloud_routine_chest", day1User?.routineId)

        val day1Guest = dao.getSplitForDayAndUserSync(1, "guest")
        assertNotNull(day1Guest)
        assertEquals("guest_routine_legs", day1Guest?.routineId)
    }

    @Test
    fun testUpdateDaySplitForUser() = runBlocking {
        val dao = db.weeklySplitDao()
        val userId = "user_test_sync"

        // Insert Monday rest day
        val mondayRest = WeeklySplitEntity(
            dayOfWeek = 1,
            routineId = null,
            routineTitle = null,
            isRestDay = true,
            userId = userId
        )
        dao.insertOrUpdate(mondayRest)

        var monday = dao.getSplitForDayAndUserSync(1, userId)
        assertTrue(monday?.isRestDay == true)

        // Update Monday with an assigned routine
        val mondayAssigned = mondayRest.copy(
            routineId = "routine_push_heavy",
            routineTitle = "Push Power",
            isRestDay = false,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdate(mondayAssigned)

        monday = dao.getSplitForDayAndUserSync(1, userId)
        assertFalse(monday?.isRestDay == true)
        assertEquals("routine_push_heavy", monday?.routineId)
        assertEquals("Push Power", monday?.routineTitle)
        assertEquals("Push Power", monday?.routineName)
    }
}
