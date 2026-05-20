package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // --- DailyRecord Queries ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyRecord(record: DailyRecord): Long

    @Update
    suspend fun updateDailyRecord(record: DailyRecord)

    @Query("SELECT * FROM daily_records WHERE date = :date")
    suspend fun getDailyRecord(date: String): DailyRecord?

    @Query("SELECT * FROM daily_records WHERE date = :date")
    fun getDailyRecordFlow(date: String): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllDailyRecordsFlow(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 30")
    fun getLast30DailyRecordsFlow(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records")
    suspend fun getAllDailyRecordsSync(): List<DailyRecord>


    // --- Prayer Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<Prayer>)

    @Update
    suspend fun updatePrayer(prayer: Prayer)

    @Query("SELECT * FROM prayers WHERE date = :date")
    fun getPrayersForDateFlow(date: String): Flow<List<Prayer>>

    @Query("SELECT * FROM prayers WHERE date = :date")
    suspend fun getPrayersForDateSync(date: String): List<Prayer>

    @Query("SELECT * FROM prayers ORDER BY date DESC")
    fun getAllPrayersFlow(): Flow<List<Prayer>>


    // --- Nafl (Daily list of user checked activities) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNawafil(nawafil: List<Nafl>)

    @Update
    suspend fun updateNafl(nafl: Nafl)

    @Query("SELECT * FROM nawafil WHERE date = :date")
    fun getNawafilForDateFlow(date: String): Flow<List<Nafl>>

    @Query("SELECT * FROM nawafil WHERE date = :date")
    suspend fun getNawafilForDateSync(date: String): List<Nafl>


    // --- Nafl Metadata (The template / settings table) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNaflMetadata(metadata: NaflMetadata): Long

    @Query("SELECT * FROM nafl_metadata")
    fun getNaflMetadataFlow(): Flow<List<NaflMetadata>>

    @Query("SELECT * FROM nafl_metadata")
    suspend fun getNaflMetadataSync(): List<NaflMetadata>

    @Update
    suspend fun updateNaflMetadata(metadata: NaflMetadata)

    @Query("DELETE FROM nafl_metadata WHERE id = :id")
    suspend fun deleteNaflMetadata(id: Long)

    @Transaction
    suspend fun createDailyRecordIfNotExist(
        date: String,
        defaultMetadata: List<NaflMetadata>
    ) {
        val existingRecord = getDailyRecord(date)
        if (existingRecord == null) {
            // Create daily record
            insertDailyRecord(DailyRecord(date = date, totalPoints = 0, isCompleted = false))

            // Pre-populate 5 prayers for this day
            val defaultPrayers = listOf(
                Prayer(date = date, prayerName = "Fajr", status = 0),
                Prayer(date = date, prayerName = "Dhuhr", status = 0),
                Prayer(date = date, prayerName = "Asr", status = 0),
                Prayer(date = date, prayerName = "Maghrib", status = 0),
                Prayer(date = date, prayerName = "Isha", status = 0)
            )
            insertPrayers(defaultPrayers)

            // Pre-populate active Nawafil based on active metadata
            val naflList = defaultMetadata.filter { it.isVisible }.map { meta ->
                Nafl(
                    date = date,
                    naflName = meta.name,
                    isCompleted = false,
                    pointWeight = meta.pointWeight
                )
            }
            if (naflList.isNotEmpty()) {
                insertNawafil(naflList)
            }
        }
    }
}
