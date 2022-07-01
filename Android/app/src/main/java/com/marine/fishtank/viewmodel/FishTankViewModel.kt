package com.marine.fishtank.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.marine.fishtank.BuildConfig
import com.marine.fishtank.ConnectionSetting
import com.marine.fishtank.DEFAULT_CONNECTION_SETTING
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "FishTankViewModel"

class FishTankViewModel(application: Application) : AndroidViewModel(application) {
    val temperatureLiveData = MutableLiveData<List<Temperature>>()
    val periodicTaskLiveData = MutableLiveData<List<PeriodicTask>>()

    private val tankApi: TankApi = TankApi.getInstance(BuildConfig.SERVER_URL)

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = _uiState

    fun init() {
        // Post first empty value to copy later.
        _uiState.postValue(
            UiState()
        )
    }

    fun readState() {
        viewModelScope.launch(Dispatchers.IO) {
            val inWaterState = tankApi.readInWaterState()
            val outWaterState = tankApi.readOutWaterState()

            _uiState.postValue(
                _uiState.value?.copy(
                    inWaterValveState = inWaterState,
                    outWaterValveState = outWaterState
                )
            )
        }
    }

    fun startFetchTemperature(days: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            temperatureLiveData.postValue(
                tankApi.readDBTemperature(days)
            )
        }
    }

    fun fetchPeriodicTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            periodicTaskLiveData.postValue(
                tankApi.fetchPeriodicTasks()
            )
        }
    }

    fun uiEvent(uiEvent: UiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiEvent) {
                is UiEvent.OutWaterEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enableOutWater(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.InWaterEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enableInWater(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.LightEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enableLight(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.HeaterEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enableHeater(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.PurifierEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enablePurifier(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.ReplaceWater -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.replaceWater(uiEvent.ratio * 0.01F)
                        readState()
                    }
                }
                is UiEvent.LedEvent -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.enableBoardLed(uiEvent.value)
                        readState()
                    }
                }
                is UiEvent.OnChangeTemperatureRange -> {
                    startFetchTemperature(uiEvent.intValue)
                }
                is UiEvent.OnLightBrightnessChange -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tankApi.changeLightBrightness(uiEvent.ratio * 0.01f)
                    }
                }
            }
        }
    }
}

data class UiState(
    var outWaterValveState: Boolean = false,
    var inWaterValveState: Boolean = false,
    var lightState: Boolean = false,
    var pumpState: Boolean = false,
    var heaterState: Boolean = false,
    var purifierState: Boolean = false,

    var temperature: Double = 0.0,
    var temperatureDays: Float = 0f,

    var resultText: String = "",

    var connectionSetting: ConnectionSetting = DEFAULT_CONNECTION_SETTING,
    var serverUrl: String = "",

    /**
     * Percentage of brightness.
     */
    var brightNess: Int = 0
)

sealed class UiEvent(
    val value: Boolean = false,
    val intValue: Int = 0
) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class HeaterEvent(enable: Boolean) : UiEvent(enable)
    class PurifierEvent(enable: Boolean) : UiEvent(enable)
    class LedEvent(enable: Boolean) : UiEvent(enable)

    class ReplaceWater(val ratio: Int) : UiEvent()
    class OnChangeTemperatureRange(count: Int) : UiEvent(intValue = count)

    class OnLightBrightnessChange(val ratio: Int): UiEvent()
}