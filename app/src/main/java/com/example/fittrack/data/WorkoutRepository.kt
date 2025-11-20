package com.example.fittrack.data


class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {

    suspend fun addWorkout(workout: WorkoutEntity) =
        workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: WorkoutEntity) =
        workoutDao.updateWorkout(workout)

    suspend fun deleteWorkout(workout: WorkoutEntity) =
        workoutDao.deleteWorkout(workout)

    suspend fun getWorkoutById(id: Long): WorkoutEntity? =
        workoutDao.getWorkoutById(id)

    suspend fun getAllWorkouts(): List<WorkoutEntity> =
        workoutDao.getAllWorkouts()
}
