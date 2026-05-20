package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.HabitDao
import com.example.data.DailyRecord
import com.example.data.Prayer
import com.example.data.Nafl
import com.example.data.NaflMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class HabitRepository(private val habitDao: HabitDao, private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("habit_tracker_prefs", Context.MODE_PRIVATE)

    fun getAppTheme(): String {
        return prefs.getString("app_theme", "system") ?: "system"
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
    }

    fun getPrayerPointsMissed(): Int {
        return prefs.getInt("prayer_pts_missed", -2)
    }

    fun setPrayerPointsMissed(value: Int) {
        prefs.edit().putInt("prayer_pts_missed", value).apply()
    }

    fun getPrayerPointsQadaa(): Int {
        return prefs.getInt("prayer_pts_qadaa", 2)
    }

    fun setPrayerPointsQadaa(value: Int) {
        prefs.edit().putInt("prayer_pts_qadaa", value).apply()
    }

    fun getPrayerPointsPresent(): Int {
        return prefs.getInt("prayer_pts_present", 5)
    }

    fun setPrayerPointsPresent(value: Int) {
        prefs.edit().putInt("prayer_pts_present", value).apply()
    }

    fun getPrayerPointsJamaah(): Int {
        return prefs.getInt("prayer_pts_jamaah", 10)
    }

    fun setPrayerPointsJamaah(value: Int) {
        prefs.edit().putInt("prayer_pts_jamaah", value).apply()
    }

    suspend fun recalculateAllDailyRecords() = withContext(Dispatchers.IO) {
        val records = habitDao.getAllDailyRecordsSync()
        records.forEach { record ->
            recalculatePointsForDate(record.date)
        }
    }


    // --- Logical Date Calculations ---
    fun getLogicalDate(timeMillis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeMillis
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        // A "Day" in this app resets at 3:00 AM (not 12:00 AM)
        if (hour < 3) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
    }

    // --- Initialization of Metadata ---
    suspend fun initDefaultMetadataIfEmpty() = withContext(Dispatchers.IO) {
        val existing = habitDao.getNaflMetadataSync()
        if (existing.isEmpty()) {
            val defaults = listOf(
                NaflMetadata(name = "صلاة الضحى", isVisible = true, pointWeight = 1, iconType = "star"),
                NaflMetadata(name = "صلاة القيام", isVisible = true, pointWeight = 1, iconType = "wind"),
                NaflMetadata(name = "أذكار الصباح", isVisible = true, pointWeight = 1, iconType = "leaf"),
                NaflMetadata(name = "أذكار المساء", isVisible = true, pointWeight = 1, iconType = "cloud"),
                NaflMetadata(name = "الرقية الشرعية", isVisible = true, pointWeight = 1, iconType = "heart"),
                NaflMetadata(name = "حفظ القرآن", isVisible = true, pointWeight = 1, iconType = "book"),
                NaflMetadata(name = "قراءة القرآن", isVisible = true, pointWeight = 1, iconType = "read"),
                NaflMetadata(name = "صيام تطوع", isVisible = true, pointWeight = 1, iconType = "flash"),
                NaflMetadata(name = "صدقة", isVisible = true, pointWeight = 1, iconType = "gift")
            )
            defaults.forEach { habitDao.insertNaflMetadata(it) }
        }
    }

    // --- Observe flows for UI ---
    fun getDailyRecordFlow(date: String): Flow<DailyRecord?> = habitDao.getDailyRecordFlow(date)
    fun getPrayersForDateFlow(date: String): Flow<List<Prayer>> = habitDao.getPrayersForDateFlow(date)
    fun getNawafilForDateFlow(date: String): Flow<List<Nafl>> = habitDao.getNawafilForDateFlow(date)
    fun getAllDailyRecordsFlow(): Flow<List<DailyRecord>> = habitDao.getAllDailyRecordsFlow()
    fun getLast30DailyRecordsFlow(): Flow<List<DailyRecord>> = habitDao.getLast30DailyRecordsFlow()
    fun getNaflMetadataFlow(): Flow<List<NaflMetadata>> = habitDao.getNaflMetadataFlow()
    fun getAllPrayersFlow(): Flow<List<Prayer>> = habitDao.getAllPrayersFlow()

    // --- Actions ---
    suspend fun ensureDailyRecordCreated(date: String) = withContext(Dispatchers.IO) {
        initDefaultMetadataIfEmpty()
        val metadata = habitDao.getNaflMetadataSync()
        habitDao.createDailyRecordIfNotExist(date, metadata)
    }

    suspend fun updatePrayerStatus(prayer: Prayer) = withContext(Dispatchers.IO) {
        habitDao.updatePrayer(prayer)
        recalculatePointsForDate(prayer.date)
    }

    suspend fun updateNaflStatus(nafl: Nafl) = withContext(Dispatchers.IO) {
        habitDao.updateNafl(nafl)
        recalculatePointsForDate(nafl.date)
    }

    // Metadata management from settings
    suspend fun addNaflMetadata(metadata: NaflMetadata) = withContext(Dispatchers.IO) {
        habitDao.insertNaflMetadata(metadata)
        // Optional: Proactively add to today's active Nawafil so the user sees it immediately
        val today = getLogicalDate()
        val existingNawafil = habitDao.getNawafilForDateSync(today)
        if (existingNawafil.none { it.naflName == metadata.name }) {
            val record = habitDao.getDailyRecord(today)
            if (record != null) {
                habitDao.insertNawafil(
                    listOf(
                        Nafl(
                            date = today,
                            naflName = metadata.name,
                            isCompleted = false,
                            pointWeight = metadata.pointWeight
                        )
                    )
                )
            }
        }
    }

    suspend fun updateNaflMetadata(metadata: NaflMetadata) = withContext(Dispatchers.IO) {
        habitDao.updateNaflMetadata(metadata)
        // Synch weights for today's record if they exist and are unchecked
        val today = getLogicalDate()
        val todayNawafil = habitDao.getNawafilForDateSync(today)
        val matching = todayNawafil.find { it.naflName == metadata.name }
        if (matching != null) {
            // update its weight
            habitDao.updateNafl(matching.copy(pointWeight = metadata.pointWeight))
            recalculatePointsForDate(today)
        }
    }

    suspend fun deleteNaflMetadata(metadataId: Long, name: String) = withContext(Dispatchers.IO) {
        habitDao.deleteNaflMetadata(metadataId)
        // Also remove from today's track if uncompleted (optional but clean behavior)
        val today = getLogicalDate()
        val todayNawafil = habitDao.getNawafilForDateSync(today)
        val matching = todayNawafil.find { it.naflName == name }
        if (matching != null && !matching.isCompleted) {
            // Delete today's entry? Let's just keep it hidden or leave it.
            // Leaving it is perfectly fine, or we can just ignore it since it is invisible now.
        }
    }

    // --- Core Recalculation Engine ---
    suspend fun recalculatePointsForDate(date: String) = withContext(Dispatchers.IO) {
        val prayers = habitDao.getPrayersForDateSync(date)
        val nawafil = habitDao.getNawafilForDateSync(date)

        val ptsMissed = getPrayerPointsMissed()
        val ptsQadaa = getPrayerPointsQadaa()
        val ptsPresent = getPrayerPointsPresent()
        val ptsJamaah = getPrayerPointsJamaah()

        var total = 0
        prayers.forEach { p ->
            total += when (p.status) {
                1 -> ptsMissed
                2 -> ptsQadaa
                3 -> ptsPresent
                4 -> ptsJamaah
                else -> 0  // Not logged
            }
        }

        nawafil.forEach { n ->
            if (n.isCompleted) {
                total += n.pointWeight
            }
        }

        val record = habitDao.getDailyRecord(date)
        if (record != null) {
            val allPrayersLogged = prayers.all { p -> p.status != 0 }
            val isCompleted = allPrayersLogged && nawafil.all { n -> n.isCompleted }
            habitDao.updateDailyRecord(
                record.copy(
                    totalPoints = total,
                    isCompleted = isCompleted
                )
            )
        }
    }
}
