package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DailyRecord
import com.example.data.Nafl
import com.example.data.NaflMetadata
import com.example.data.Prayer
import com.example.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HabitRepository(db.habitDao(), application)

    // --- State: App Theme ---
    private val _appTheme = MutableStateFlow(repository.getAppTheme())
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    // --- State: Configurable Prayer Points ---
    private val _ptsMissed = MutableStateFlow(repository.getPrayerPointsMissed())
    val ptsMissed: StateFlow<Int> = _ptsMissed.asStateFlow()

    private val _ptsQadaa = MutableStateFlow(repository.getPrayerPointsQadaa())
    val ptsQadaa: StateFlow<Int> = _ptsQadaa.asStateFlow()

    private val _ptsPresent = MutableStateFlow(repository.getPrayerPointsPresent())
    val ptsPresent: StateFlow<Int> = _ptsPresent.asStateFlow()

    private val _ptsJamaah = MutableStateFlow(repository.getPrayerPointsJamaah())
    val ptsJamaah: StateFlow<Int> = _ptsJamaah.asStateFlow()

    // --- State: Selected Date ---
    private val _selectedDate = MutableStateFlow(repository.getLogicalDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        // Run database initialization and pre-populate metadata
        viewModelScope.launch {
            repository.initDefaultMetadataIfEmpty()
            // Ensure today's record exists
            repository.ensureDailyRecordCreated(repository.getLogicalDate())
        }
    }

    // --- Dynamic Streams based on Selected Date ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentDailyRecord: StateFlow<DailyRecord?> = _selectedDate
        .flatMapLatest { date ->
            // Ensure record exists before reading
            repository.ensureDailyRecordCreated(date)
            repository.getDailyRecordFlow(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentPrayers: StateFlow<List<Prayer>> = _selectedDate
        .flatMapLatest { date ->
            repository.getPrayersForDateFlow(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentNawafil: StateFlow<List<Nafl>> = _selectedDate
        .flatMapLatest { date ->
            repository.getNawafilForDateFlow(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Historic Streams ---
    val last30DailyRecords: StateFlow<List<DailyRecord>> = repository.getLast30DailyRecordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val naflMetadata: StateFlow<List<NaflMetadata>> = repository.getNaflMetadataFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPrayers: StateFlow<List<Prayer>> = repository.getAllPrayersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Setter ---
    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectToday() {
        _selectedDate.value = repository.getLogicalDate()
    }

    // --- Operations ---
    fun updatePrayer(prayer: Prayer, newStatus: Int) {
        viewModelScope.launch {
            repository.updatePrayerStatus(prayer.copy(status = newStatus))
        }
    }

    fun toggleNafl(nafl: Nafl) {
        viewModelScope.launch {
            repository.updateNaflStatus(nafl.copy(isCompleted = !nafl.isCompleted))
        }
    }

    // --- Settings Operations ---
    fun addNaflMetadata(name: String, weight: Int, iconType: String) {
        viewModelScope.launch {
            repository.addNaflMetadata(
                NaflMetadata(
                    name = name,
                    pointWeight = weight,
                    iconType = iconType,
                    isVisible = true
                )
            )
        }
    }

    fun updateNaflMetadataVisibility(metadata: NaflMetadata, isVisible: Boolean) {
        viewModelScope.launch {
            repository.updateNaflMetadata(metadata.copy(isVisible = isVisible))
        }
    }

    fun updateNaflMetadataWeight(metadata: NaflMetadata, newWeight: Int) {
        val safeWeight = newWeight.coerceAtLeast(1)
        viewModelScope.launch {
            repository.updateNaflMetadata(metadata.copy(pointWeight = safeWeight))
        }
    }

    fun deleteNaflMetadata(metadata: NaflMetadata) {
        viewModelScope.launch {
            repository.deleteNaflMetadata(metadata.id, metadata.name)
        }
    }

    fun updateAppTheme(theme: String) {
        repository.setAppTheme(theme)
        _appTheme.value = theme
    }

    fun updatePrayerPoints(missed: Int, qadaa: Int, present: Int, jamaah: Int) {
        repository.setPrayerPointsMissed(missed)
        repository.setPrayerPointsQadaa(qadaa)
        repository.setPrayerPointsPresent(present)
        repository.setPrayerPointsJamaah(jamaah)

        _ptsMissed.value = missed
        _ptsQadaa.value = qadaa
        _ptsPresent.value = present
        _ptsJamaah.value = jamaah

        viewModelScope.launch {
            repository.recalculateAllDailyRecords()
            repository.recalculatePointsForDate(_selectedDate.value)
        }
    }

    // --- Dynamic Computation Helpers ---
    fun getStreak(records: List<DailyRecord>): Int {
        if (records.isEmpty()) return 0
        val sorted = records.sortedByDescending { it.date }
        val today = repository.getLogicalDate()
        var streak = 0
        var expectedDateCalendar = java.util.Calendar.getInstance()
        
        // Find if today is in the records
        val hasActivityToday = sorted.find { it.date == today && it.totalPoints > 0 } != null
        
        // If no activity today, check if active yesterday (streak lives until the end of the day)
        if (!hasActivityToday) {
            expectedDateCalendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        
        var checking = true
        while (checking) {
            val yr = expectedDateCalendar.get(java.util.Calendar.YEAR)
            val mo = expectedDateCalendar.get(java.util.Calendar.MONTH) + 1
            val dy = expectedDateCalendar.get(java.util.Calendar.DAY_OF_MONTH)
            val dateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", yr, mo, dy)
            
            val record = sorted.find { it.date == dateStr }
            if (record != null && record.totalPoints > 0) {
                streak++
                expectedDateCalendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                checking = false
            }
        }
        return streak
    }

    fun getBestDayPoints(records: List<DailyRecord>): Int {
        return records.maxOfOrNull { it.totalPoints } ?: 0
    }
}
