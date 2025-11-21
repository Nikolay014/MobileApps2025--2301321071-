package com.example.fittrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val type: String,
    val dateTime: Long,
    val durationMinutes: Int?,
    val distanceKm: Double?,
    val sets: Int?,
    val reps: Int?,
    val photoUri: String?,
    val latitude: Double?,
    val longitude: Double?,
    val notes: String?,
    val startAddress: String? = null,
    val startLatitude: Double? = null,
    val startLongitude: Double? = null

)
