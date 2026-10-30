package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.RoutineEntity
import com.example.ui.screens.RoutineTemplateExercise
import com.example.ui.screens.RoutineTemplateSet
import com.example.ui.screens.parseRoutineExercises
import com.example.ui.screens.serializeExercisesToJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RoutineEditAndSyncTest {

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
    fun testUpdateRoutineAtomicallyInRoom() = runBlocking {
        val dao = db.routineDao()

        // 1. Initial routine creation for user_123
        val initial = RoutineEntity(
            id = "routine_back_tri",
            userId = "user_123",
            title = "Back & Tricep",
            description = "Initial routine notes",
            exercisesSummary = "Lat Pulldown (Cable) (3 sets), Barbell Bent Over Row (3 sets)",
            exercisesJson = "",
            sortOrder = 0,
            createdAt = 1000L
        )
        dao.insert(initial)

        val retrievedBefore = dao.getRoutineById("routine_back_tri")
        assertNotNull(retrievedBefore)
        assertEquals("Back & Tricep", retrievedBefore?.title)
        assertEquals("Initial routine notes", retrievedBefore?.description)

        // 2. Simulate editing the routine (title, notes, summary, json)
        val updated = RoutineEntity(
            id = "routine_back_tri",
            userId = "user_123",
            title = "Back & Tricep Elite",
            description = "Heavy rows, high volume pulldowns and dips",
            exercisesSummary = "Lat Pulldown (Cable) (4 sets), Barbell Bent Over Row (4 sets), Tricep Rope Pushdown (3 sets)",
            exercisesJson = "{\"v\":2,\"name\":\"Back & Tricep Elite\"}",
            sortOrder = 0,
            createdAt = 1000L
        )
        dao.insert(updated)

        // Verify updated in Room atomically without duplicate records
        val userRoutines = dao.getRoutinesForUser("user_123").first()
        assertEquals(1, userRoutines.size)
        val saved = userRoutines.first()
        assertEquals("routine_back_tri", saved.id)
        assertEquals("Back & Tricep Elite", saved.title)
        assertEquals("Heavy rows, high volume pulldowns and dips", saved.description)
        assertEquals("user_123", saved.userId)
        assertTrue(saved.exercisesSummary.contains("Tricep Rope Pushdown"))
    }

    @Test
    fun testParseRoutineExercisesFromSummaryAndJson() {
        val dbExercises = listOf(
            ExerciseEntity(id = "1", name = "Lat Pulldown (Cable)", primaryMuscle = "Back", equipmentType = "Cable"),
            ExerciseEntity(id = "2", name = "Barbell Bent Over Row", primaryMuscle = "Back", equipmentType = "Barbell"),
            ExerciseEntity(id = "3", name = "Tricep Rope Pushdown", primaryMuscle = "Triceps", equipmentType = "Cable")
        )

        // Routine with exercisesSummary only
        val summaryRoutine = RoutineEntity(
            id = "r1",
            title = "Back Day",
            exercisesSummary = "Lat Pulldown (Cable) (4 sets), Barbell Bent Over Row (3 sets)",
            exercisesJson = ""
        )

        val parsedFromSummary = parseRoutineExercises(summaryRoutine, dbExercises)
        assertEquals(2, parsedFromSummary.size)
        assertEquals("Lat Pulldown (Cable)", parsedFromSummary[0].name)
        assertEquals(4, parsedFromSummary[0].sets.size)
        assertEquals("Barbell Bent Over Row", parsedFromSummary[1].name)
        assertEquals(3, parsedFromSummary[1].sets.size)

        // Now serialize with customized weights & reps
        val templateExercises = listOf(
            RoutineTemplateExercise(
                id = "ex_1",
                name = "Lat Pulldown (Cable)",
                primaryMuscle = "Back",
                equipmentType = "Cable",
                sets = mutableListOf(
                    RoutineTemplateSet(weight = "65", reps = "12"),
                    RoutineTemplateSet(weight = "70", reps = "10")
                )
            )
        )
        val json = serializeExercisesToJson("Back Day", "Notes", templateExercises)
        assertTrue(json.contains("Lat Pulldown (Cable)"))

        // Parse from JSON
        val jsonRoutine = RoutineEntity(
            id = "r2",
            title = "Back Day",
            exercisesSummary = "Lat Pulldown (Cable) (2 sets)",
            exercisesJson = json
        )
        val parsedFromJson = parseRoutineExercises(jsonRoutine, dbExercises)
        assertEquals(1, parsedFromJson.size)
        assertEquals("Lat Pulldown (Cable)", parsedFromJson[0].name)
        assertEquals(2, parsedFromJson[0].sets.size)
        assertEquals("65", parsedFromJson[0].sets[0].weight)
        assertEquals("12", parsedFromJson[0].sets[0].reps)
    }

    @Test
    fun testUserIsolationForRoutines() = runBlocking {
        val dao = db.routineDao()

        // Routine for guest
        val guestRoutine = RoutineEntity(
            id = "r_guest",
            userId = "guest",
            title = "Guest Routine",
            description = "Guest workout"
        )
        dao.insert(guestRoutine)

        // Routine for authenticated user
        val userRoutine = RoutineEntity(
            id = "r_auth",
            userId = "user_cloud_99",
            title = "Cloud Routine",
            description = "Synced workout"
        )
        dao.insert(userRoutine)

        val guestList = dao.getRoutinesForUser("guest").first()
        val authList = dao.getRoutinesForUser("user_cloud_99").first()

        assertEquals(1, guestList.size)
        assertEquals("Guest Routine", guestList.first().title)

        assertEquals(1, authList.size)
        assertEquals("Cloud Routine", authList.first().title)
    }

    @Test
    fun testReorderRoutinesAndSortOrder() = runBlocking {
        val dao = db.routineDao()

        val r1 = RoutineEntity(id = "r_leg", userId = "u1", title = "Legs", sortOrder = 0)
        val r2 = RoutineEntity(id = "r_push", userId = "u1", title = "Push", sortOrder = 1)
        val r3 = RoutineEntity(id = "r_pull", userId = "u1", title = "Pull", sortOrder = 2)
        dao.insertAll(listOf(r1, r2, r3))

        val initialList = dao.getRoutinesForUser("u1").first()
        assertEquals(listOf("Legs", "Push", "Pull"), initialList.map { it.title })

        // Reorder: Pull (0), Legs (1), Push (2)
        dao.updateAllRoutineOrders(listOf("r_pull" to 0, "r_leg" to 1, "r_push" to 2))

        val reorderedList = dao.getRoutinesForUser("u1").first()
        assertEquals(listOf("Pull", "Legs", "Push"), reorderedList.map { it.title })
        assertEquals(0, reorderedList[0].sortOrder)
        assertEquals(1, reorderedList[1].sortOrder)
        assertEquals(2, reorderedList[2].sortOrder)
    }
}
