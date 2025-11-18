package com.example.fittrack.data

import androidx.room.*

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts ORDER BY dateTime DESC")
    suspend fun getAllWorkouts(): List<WorkoutEntity>

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?
}
