package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val date: String, // String "yyyy-MM-dd"
    val totalPoints: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "prayers",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecord::class,
            parentColumns = ["date"],
            childColumns = ["date"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["date"])]
)
data class Prayer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val prayerName: String, // "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"
    val status: Int // 0 = Not Selected (0pts), 1 = Missed/لم أصلِ (-2pts), 2 = Qadaa/قضاء (2pts), 3 = Present/حاضر (5pts), 4 = Jamaah/جماعة (10pts)
)

@Entity(
    tableName = "nafl_metadata"
)
data class NaflMetadata(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isVisible: Boolean = true,
    val pointWeight: Int = 1,
    val iconType: String = "star" // icon identifier: star, wind, leaf, cloud, heart, book, read, flash, gift
)

@Entity(
    tableName = "nawafil",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecord::class,
            parentColumns = ["date"],
            childColumns = ["date"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["date"])]
)
data class Nafl(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val naflName: String,
    val isCompleted: Boolean = false,
    val pointWeight: Int = 1
)
