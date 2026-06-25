package com.ph.processchecker.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ph.processchecker.App
import com.ph.processchecker.base.MVI
import com.ph.processchecker.base.MVIDelegate
import com.ph.processchecker.ext.hasNotificationPermission
import com.ph.processchecker.model.AppInfo
import com.ph.processchecker.util.ProcessUtil
import com.ph.processchecker.util.sp.AppSp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(),
    MVI<MainContract.UiState, MainContract.UiIntent, MainContract.UiEffect> by MVIDelegate(
        MainContract.UiState()
    ) {
    companion object {
        private const val TAG = "MainViewModel"
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            uiIntentFlow.collect { uiIntent ->
                when (uiIntent) {
                    MainContract.UiIntent.Refresh -> refreshData()

                    is MainContract.UiIntent.ChangeShowUsageStatsPermissionBottomSheet -> {
                        setUiState { copy(showUsageStatsPermissionBottomSheet = uiIntent.show) }
                    }

                    is MainContract.UiIntent.ChangeCheckedNotifySwitch -> {
                        AppSp.notifySwitch = uiIntent.checked
                        setUiState { copy(checkedNotifySwitch = uiIntent.checked) }
                    }
                }
            }
        }
    }

    private fun refreshData() {
        setUiState { copy(loading = true) }
        viewModelScope.launch {
            val check = App.instance.hasNotificationPermission() && AppSp.notifySwitch
            val data = ProcessUtil.getRunningProcess()
            Log.d(TAG, "running apps: \n${data.joinToString("\n") { it.appName }}")
            setUiState {
                copy(
                    loading = false,
                    data = data,
                    checkedNotifySwitch = check
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
        val checkedNotifySwitch: Boolean = false,
    )

    sealed interface UiIntent {
        data object Refresh : UiIntent
        data class ChangeShowUsageStatsPermissionBottomSheet(val show: Boolean) : UiIntent
        data class ChangeCheckedNotifySwitch(val checked: Boolean) : UiIntent
    }

    sealed interface UiEffect
}