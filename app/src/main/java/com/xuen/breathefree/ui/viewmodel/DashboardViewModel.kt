package com.xuen.breathefree.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xuen.breathefree.data.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalSessions: Int = 0,
    val daysClean: Long = 0,
    val weeklyStats: List<Int> = emptyList()
)

class DashboardViewModel(
    private val repository: UserStatsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.totalSessions,
        repository.daysClean,
        repository.weeklyStats
    ) { sessions, days, weekly ->
        DashboardUiState(
            totalSessions = sessions,
            daysClean = days,
            weeklyStats = weekly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun onSessionCompleted() {
        repository.incrementSession()
    }
}
