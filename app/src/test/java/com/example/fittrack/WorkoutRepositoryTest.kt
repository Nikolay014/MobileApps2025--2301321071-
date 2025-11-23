package com.example.fittrack

import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.data.WorkoutRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkoutRepositoryTest {

    private lateinit var fakeDao: FakeWorkoutDao
    private lateinit var repository: WorkoutRepository

    @Before
    fun setup() {
        fakeDao = FakeWorkoutDao()
        repository = WorkoutRepository(fakeDao)
    }

    // малко helper, за да не повтаряме код
    private fun createSampleWorkout(
        id: Long = 0L,
        title: String = "Run",
        type: String = "run",
        dateTime: Long = 1000L
    ): WorkoutEntity {
        return WorkoutEntity(
            id = id,
            title = title,
            type = type,
            dateTime = dateTime,
            durationMinutes = 30,
            distanceKm = 5.0,
            sets = null,
            reps = null,
            photoUri = null,
            latitude = null,
            longitude = null,
            notes = "test",
            startAddress = "Somewhere",
            startLatitude = 42.0,
            startLongitude = 23.0
        )
    }

    @Test
    fun addWorkout_increasesSize() = runBlocking {
        // given
        val w1 = createSampleWorkout(title = "Run 1")

        // when
        repository.addWorkout(w1)
        val all = repository.getAllWorkouts()

        // then
        assertEquals(1, all.size)
        assertEquals("Run 1", all.first().title)
    }

    @Test
    fun updateWorkout_changesFields() = runBlocking {
        // given
        val original = createSampleWorkout(title = "Old title")
        repository.addWorkout(original)
        val saved = repository.getAllWorkouts().first()
        val updated = saved.copy(title = "New title", durationMinutes = 45)

        // when
        repository.updateWorkout(updated)
        val reloaded = repository.getWorkoutById(saved.id)!!

        // then
        assertEquals("New title", reloaded.title)
        assertEquals(45, reloaded.durationMinutes)
    }

    @Test
    fun deleteWorkout_removesItem() = runBlocking {
        // given
        val w1 = createSampleWorkout(title = "Run 1", dateTime = 1000L)
        val w2 = createSampleWorkout(title = "Run 2", dateTime = 2000L)
        repository.addWorkout(w1)
        repository.addWorkout(w2)
        val allBefore = repository.getAllWorkouts()
        assertEquals(2, allBefore.size)

        // when
        repository.deleteWorkout(allBefore.first())

        // then
        val allAfter = repository.getAllWorkouts()
        assertEquals(1, allAfter.size)
        assertTrue(allAfter.any { it.title == "Run 2" })
    }
    @Test
    fun getWorkoutById_returnsCorrectItem() = runBlocking {
        // given
        repository.addWorkout(createSampleWorkout(title = "Run 1"))
        repository.addWorkout(createSampleWorkout(title = "Run 2"))

        val all = repository.getAllWorkouts()
        val second = all[1]

        // when
        val loaded = repository.getWorkoutById(second.id)

        // then
        assertNotNull(loaded)
        assertEquals("Run 2", loaded!!.title)
        assertEquals(second.id, loaded.id)
    }

    @Test
    fun insert_assignsAutoIncrementId() = runBlocking {
        // given
        val w = createSampleWorkout(id = 0L, title = "Auto ID")

        // when
        repository.addWorkout(w)
        val saved = repository.getAllWorkouts().first()

        // then – очакваме id да не е 0 (симулация на auto-increment)
        assertNotEquals(0L, saved.id)
        assertEquals("Auto ID", saved.title)
    }

    @Test
    fun deleteAllResultsInEmptyList() = runBlocking {
        // given
        repository.addWorkout(createSampleWorkout(title = "W1"))
        repository.addWorkout(createSampleWorkout(title = "W2"))
        val before = repository.getAllWorkouts()
        assertEquals(2, before.size)

        // when – трием всички един по един
        before.forEach { repository.deleteWorkout(it) }

        // then
        val after = repository.getAllWorkouts()
        assertTrue(after.isEmpty())
    }
}
