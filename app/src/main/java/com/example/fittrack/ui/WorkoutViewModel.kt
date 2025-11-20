package com.example.fittrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.fittrack.data.AppDatabase
import com.example.fittrack.data.WorkoutEntity
import com.example.fittrack.data.WorkoutRepository
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoutRepository

    // LiveData със списъка от тренировки (за Dashboard)
    private val _workouts = MutableLiveData<List<WorkoutEntity>>()
    val workouts: LiveData<List<WorkoutEntity>> get() = _workouts

    init {
        // взимаме базата и DAO-то
        val db = AppDatabase.getInstance(application)
        val workoutDao = db.workoutDao()
        repository = WorkoutRepository(workoutDao)

        // зареждаме тренировките от базата
        viewModelScope.launch {
            loadWorkouts()
        }
    }

    private suspend fun loadWorkouts() {
        _workouts.value = repository.getAllWorkouts()
    }

    // Публичен метод за ре-зареждане (например след добавяне)
    fun refreshWorkouts() {
        viewModelScope.launch {
            loadWorkouts()
        }
    }


    fun addWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.addWorkout(workout)
            loadWorkouts() // след добавяне, презареждаме списъка
        }
    }
    fun updateWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.updateWorkout(workout)
            loadWorkouts()
        }
    }

    fun deleteWorkout(workout: WorkoutEntity) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
            loadWorkouts()
        }
    }


    suspend fun getWorkoutById(id: Long): WorkoutEntity? = repository.getWorkoutById(id)



    // По-късно можем да добавим и update/delete
}
