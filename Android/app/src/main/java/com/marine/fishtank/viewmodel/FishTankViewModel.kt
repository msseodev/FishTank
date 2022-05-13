package com.marine.fishtank.viewmodel

import androidx.compose.runtime.Stable
import androidx.lifecycle.*
import kotlinx.coroutines.launch

data class UiState(
    var outWaterValveState: Boolean,
    var inWaterValveState: Boolean,
    var lightState: Boolean,
    var pumpState: Boolean,
    var heaterState: Boolean = false,
    var purifierState: Boolean = false,

    var resultText: String = "",
)

sealed class UiEvent(val value: Boolean = false) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class PumpEvent(enable: Boolean) : UiEvent(enable)
    class HeaterEvent(enable: Boolean) : UiEvent(enable)
    class PurifierEvent(enable: Boolean) : UiEvent(enable)

    class ChangeWater : UiEvent()
}

class FishTankViewModel : ViewModel() {
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = _uiState

    fun fetchState() {
        viewModelScope.launch {
            // Some Fetches occur...
            _uiState.value = UiState(
                outWaterValveState = true,
                inWaterValveState = false,
                lightState = true,
                pumpState = true,
                resultText = "Fetch complete!"
            )
        }
    }

    fun uiEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            is UiEvent.OutWaterEvent -> {
                _uiState.value = _uiState.value?.copy(
                    resultText = "${if (uiEvent.value) "Open" else "Close"} Out-Water valve!",
                    outWaterValveState = uiEvent.value,
                )
            }
            is UiEvent.InWaterEvent -> {
                _uiState.value = _uiState.value?.copy(
                    inWaterValveState = uiEvent.value,
                    resultText = "${if (uiEvent.value) "Open" else "Close"} In-Water valve!"
                )
            }
            is UiEvent.LightEvent -> {
                _uiState.value = _uiState.value?.copy(
                    lightState = uiEvent.value,
                    resultText = "Light ${if (uiEvent.value) "On" else "Off"} "
                )
            }
            is UiEvent.PumpEvent -> {
                _uiState.value = _uiState.value?.copy(
                    pumpState = uiEvent.value,
                    resultText = "Pump ${if (uiEvent.value) "On" else "Off"} "
                )
            }
            is UiEvent.HeaterEvent -> {
                _uiState.value = _uiState.value?.copy(
                    heaterState = uiEvent.value,
                    resultText = "Heater ${if (uiEvent.value) "On" else "Off"} "
                )
            }
            is UiEvent.PurifierEvent -> {
                _uiState.value = _uiState.value?.copy(
                    purifierState = uiEvent.value,
                    resultText = "Purifier ${if (uiEvent.value) "On" else "Off"} "
                )
            }
            is UiEvent.ChangeWater -> {
                _uiState.value = _uiState.value?.copy(
                    resultText = "Start change-water"
                )
            }
        }
    }

}


class FishTankViewModelFactory() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishTankViewModel::class.java)) {
            return FishTankViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}