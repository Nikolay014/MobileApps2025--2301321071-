package com.example.fittrack

import com.example.fittrack.data.WorkoutDao
import com.example.fittrack.data.WorkoutEntity

/**
 * Fake имплементация на WorkoutDao за unit тестове.
 * Няма Room, няма база – само in-memory списък.
 */
class FakeWorkoutDao : WorkoutDao {

    private val items = mutableListOf<WorkoutEntity>()
    private var nextId = 1L

    override suspend fun getAllWorkouts(): List<WorkoutEntity> {
        return items.toList()
    }

    override suspend fun getWorkoutById(id: Long): WorkoutEntity? {
        return items.firstOrNull { it.id == id }
    }

    override suspend fun insertWorkout(workout: WorkoutEntity) {
        // Ако id == 0, симулираме auto-increment
        val realId = if (workout.id == 0L) nextId++ else workout.id
        val toSave = workout.copy(id = realId)
        items.add(toSave)
    }

    override suspend fun updateWorkout(workout: WorkoutEntity) {
        val index = items.indexOfFirst { it.id == workout.id }
        if (index != -1) {
            items[index] = workout
        }
    }

    override suspend fun deleteWorkout(workout: WorkoutEntity) {
        items.removeAll { it.id == workout.id }
    }
}
