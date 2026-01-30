package com.xuen.breathefree.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class UserStatsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_stats", Context.MODE_PRIVATE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _totalSessions = MutableStateFlow(0)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    private val _daysClean = MutableStateFlow(0L)
    val daysClean: StateFlow<Long> = _daysClean.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<Int>>(emptyList())
    val weeklyStats: StateFlow<List<Int>> = _weeklyStats.asStateFlow()

    private val _selectedTheme = MutableStateFlow("Inferno")
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        _totalSessions.value = prefs.getInt("total_sessions", 0)
        
        val startDateStr = prefs.getString("start_date", null)
        if (startDateStr == null) {
            // First time launch, set start date to today
            val today = LocalDate.now().format(dateFormatter)
            prefs.edit().putString("start_date", today).apply()
            _daysClean.value = 0
        } else {
            val startDate = LocalDate.parse(startDateStr, dateFormatter)
            val today = LocalDate.now()
            _daysClean.value = ChronoUnit.DAYS.between(startDate, today)
        }

        refreshWeeklyStats()
        _selectedTheme.value = prefs.getString("selected_theme", "Inferno") ?: "Inferno"
    }

    fun incrementSession() {
        val currentTotal = _totalSessions.value + 1
        _totalSessions.value = currentTotal
        
        val todayStr = LocalDate.now().format(dateFormatter)
        val currentDailyCount = prefs.getInt("daily_$todayStr", 0)
        
        prefs.edit()
            .putInt("total_sessions", currentTotal)
            .putInt("daily_$todayStr", currentDailyCount + 1)
            .apply()

        refreshWeeklyStats()
    }

    private fun refreshWeeklyStats() {
        val today = LocalDate.now()
        val stats = mutableListOf<Int>()
        // Get last 7 days including today
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.format(dateFormatter)
            stats.add(prefs.getInt("daily_$dateStr", 0))
        }
        _weeklyStats.value = stats
    }
    
    fun setTheme(themeName: String) {
        _selectedTheme.value = themeName
        prefs.edit().putString("selected_theme", themeName).apply()
    }
}
