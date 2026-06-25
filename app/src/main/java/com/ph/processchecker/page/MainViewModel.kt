package com.ph.processchecker.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ph.processchecker.base.MVI
import com.ph.processchecker.base.MVIDelegate
import com.ph.processchecker.model.AppInfo
import com.ph.processchecker.util.ProcessUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(),
    MVI<MainContract.UiState, MainContract.UiIntent, MainContract.UiEffect> by MVIDelegate(
        MainContract.UiState()
    ) {
    init {
        viewModelScope.launch(Dispatchers.IO) {
            uiIntentFlow.collect { uiIntent ->
                when (uiIntent) {
                    MainContract.UiIntent.Refresh -> refreshData()

                    is MainContract.UiIntent.ChangeShowUsageStatsPermissionBottomSheet -> {
                        setUiState { copy(showUsageStatsPermissionBottomSheet = uiIntent.show) }
                    }
                }
            }
        }
    }

    private fun refreshData() {
        setUiState { copy(loading = true) }
        viewModelScope.launch {
            val data = ProcessUtil.getRunningProcess()
            setUiState {
                copy(
                    loading = false,
                    data = data
                )
            }
        }
    }

}

class MainContract {
    data class UiState(
        val loading: Boolean = true,
        val data: List<AppInfo> = emptyList(),
        val showUsageStatsPermissionBottomSheet: Boolean = false,
    )

    sealed interface UiIntent {
        data object Refresh : UiIntent
        data class ChangeShowUsageStatsPermissionBottomSheet(val show: Boolean) : UiIntent
    }

    sealed interface UiEffect
}