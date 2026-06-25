package com.ph.processchecker.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface MVI<UiState, UiIntent, UiEffect> {
    val uiStateFlow: StateFlow<UiState>
    val uiIntentFlow: Flow<UiIntent>
    val uiEffectFlow: Flow<UiEffect>

    fun setUiState(produce: UiState.() -> UiState)
    suspend fun setUiIntent(uiIntent: UiIntent)
    suspend fun setUiEffect(uiEffect: UiEffect)

    fun sendUiIntent(
        uiIntent: UiIntent,
        coroutineScope: CoroutineScope,
        coroutineContext: CoroutineContext = Dispatchers.IO,
    ) {
        coroutineScope.launch(coroutineContext) {
            setUiIntent(uiIntent)
        }
    }

    fun ViewModel.sendUiEffect(
        uiEffect: UiEffect,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ) {
        viewModelScope.launch(coroutineContext) {
            setUiEffect(uiEffect)
        }
    }
}

class MVIDelegate<UiState, UiIntent, UiEffect>(
    initialUiState: UiState
) : MVI<UiState, UiIntent, UiEffect> {
    private val _uiStateFlow = MutableStateFlow(initialUiState)
    override val uiStateFlow: StateFlow<UiState>
        get() = _uiStateFlow.asStateFlow()

    private val _uiIntentChannel = Channel<UiIntent>()
    override val uiIntentFlow: Flow<UiIntent>
        get() = _uiIntentChannel.receiveAsFlow()

    private val _uiEffectChannel: Channel<UiEffect> = Channel()
    override val uiEffectFlow: Flow<UiEffect>
        get() = _uiEffectChannel.receiveAsFlow()

    override fun setUiState(produce: UiState.() -> UiState) {
        val newUiState = _uiStateFlow.value.produce()
        _uiStateFlow.value = newUiState
    }

    override suspend fun setUiIntent(uiIntent: UiIntent) {
        _uiIntentChannel.send(uiIntent)
    }

    override suspend fun setUiEffect(uiEffect: UiEffect) {
        _uiEffectChannel.send(uiEffect)
    }
}