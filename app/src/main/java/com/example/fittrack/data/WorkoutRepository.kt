package com.example.fittrack.data


class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {

    suspend fun addWorkout(workout: WorkoutEntity) {
        workoutDao.insertWorkout(workout)
    }

    suspend fun getAllWorkouts(): List<WorkoutEntity> {
        return workoutDao.getAllWorkouts()
    }

    suspend fun getWorkoutById(id: Long): WorkoutEntity? {
        return workoutDao.getWorkoutById(id)
    }

    suspend fun updateWorkout(workout: WorkoutEntity) {
        workoutDao.updateWorkout(workout)
    }

    suspend fun deleteWorkout(workout: WorkoutEntity) {
        workoutDao.deleteWorkout(workout)
    }
}
