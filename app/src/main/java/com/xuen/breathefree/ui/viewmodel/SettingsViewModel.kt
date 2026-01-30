package com.xuen.breathefree.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.xuen.breathefree.data.UserStatsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val repository: UserStatsRepository) : ViewModel() {
    val selectedTheme: StateFlow<String> = repository.selectedTheme

    fun updateTheme(themeName: String) {
        repository.setTheme(themeName)
    }
}
