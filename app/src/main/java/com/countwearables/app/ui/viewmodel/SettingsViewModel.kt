package com.countwearables.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.countwearables.app.data.repository.AuthRepository
import com.countwearables.app.data.repository.BackupRepository
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val backupRepository = BackupRepository(application)

    fun exportData(file: File) {
        val userId = authRepository.getCurrentUserId()
        viewModelScope.launch {
            backupRepository.exportToJson(userId, file)
        }
    }

    fun importData(json: String) {
        val userId = authRepository.getCurrentUserId()
        viewModelScope.launch {
            backupRepository.importFromJson(userId, json)
        }
    }
}
