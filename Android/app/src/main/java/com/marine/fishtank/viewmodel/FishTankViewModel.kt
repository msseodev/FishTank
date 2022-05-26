package com.marine.fishtank.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.marine.fishtank.api.TankApi
import com.marine.fishtank.api.TankApiImpl
import com.marine.fishtank.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round

private const val TAG = "FishTankViewModel"
private const val SERVER_URL = "marineseo.iptime.org"
//private const val SERVER_URL = "192.168.0.12"
private const val SERVER_PORT = 53265

// 어항 물 용량
private const val TANK_WATER_VOLUME = 100

data class UiState(
    var outWaterValveState: Boolean = false,
    var inWaterValveState: Boolean = false,
    var lightState: Boolean = false,
    var pumpState: Boolean = false,
    var heaterState: Boolean = false,
    var purifierState: Boolean = false,

    var temperature: Double = 0.0,

    var resultText: String = "",
)

sealed class UiEvent(val value: Boolean = false) {
    class OutWaterEvent(enable: Boolean) : UiEvent(enable)
    class InWaterEvent(enable: Boolean) : UiEvent(enable)
    class LightEvent(enable: Boolean) : UiEvent(enable)
    class PumpEvent(enable: Boolean) : UiEvent(enable)
    class HeaterEvent(enable: Boolean) : UiEvent(enable)
    class PurifierEvent(enable: Boolean) : UiEvent(enable)
    class LedEvent(enable: Boolean) : UiEvent(enable)

    class ChangeWater : UiEvent()
}

class FishTankViewModel : ViewModel() {
    val temperatureLiveData = MutableLiveData<List<Temperature>>()

    val initializeLiveData = MutableLiveData<Boolean>()

    private val tankApi: TankApi = TankApiImpl()

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = _uiState

    private val packetListener = object : TankApi.OnServerPacketListener {
        override fun onServerPacket(packet: ServerPacket) {
            // packet sent by server.
            Log.d(TAG, "onServerPacket=$packet")
            when (packet.opCode) {
                SERVER_OP_READ_TEMPERATURE -> {
                    // only one temperature!
                    if(packet.temperatureList.isNotEmpty()) {
                        val list = mutableListOf<Temperature>()
                        list.add(packet.temperatureList[0])
                        temperatureLiveData.postValue(
                            list
                        )
                    }
                }
                SERVER_OP_DB_TEMPERATURE -> {
                    // List of temperature!
                    temperatureLiveData.postValue(packet.temperatureList)
                }
            }
        }
    }

    fun init() {
        // Post first empty value to copy later.
        _uiState.postValue(
            UiState()
        )

        viewModelScope.launch(Dispatchers.IO) {
            val connectResult = tankApi.connect(SERVER_URL, SERVER_PORT)
            withContext(Dispatchers.Main) {
                initializeLiveData.value = connectResult
            }

            tankApi.registerServerPacketListener(packetListener)
        }
    }

    fun startFetchTemperature() {
        viewModelScope.launch(Dispatchers.IO) {
            tankApi.sendCommand(ServerPacket(AppId.MY_ID, SERVER_OP_DB_TEMPERATURE))
        }
    }

    private fun changeWater(ratio: Double) {
        // TODO - End-peer 에서 모든걸 제어하는건 너무 위험하다.. 도중에 연결이 끊어지면????
        //      - 아래 내용 전부를 서버에 보내고 서버에서 처리해야 한다.
        viewModelScope.launch(Dispatchers.IO) {
            // Before draining we have to close in-water solenoid valve.
            enableInWaterValve(false)

            // Open out-water valve.
            enableOutWaterValve(true)
            // Wait some delay.
            delay(1000L * 2)

            val pumpOperationTime = calculateWaterPumpTime(ratio)
            Log.d(TAG, "Pump operation time=$pumpOperationTime seconds.")

            // Start water-pump!
            enablePump(true)
            delay(pumpOperationTime * 1000L)

            // Stop water-pump!
            enablePump(false)
            delay(1000L * 2)

            // Close out-water valve. Water draining is done!
            enableOutWaterValve(false)

            // Now we have to supply water. We assume that in-water has ball-top valve.
            enableInWaterValve(true)
        }
    }

    private fun enableOutWaterValve(open: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_OUT_WATER, data = if (open) 1 else 0))
    }

    private fun enableInWaterValve(open: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_IN_WATER, data = if (open) 1 else 0))
    }

    private fun enablePump(run: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_WATER_PUMP, data = if (run) 1 else 0))
    }

    private fun enableLight(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_LIGHT, data = if (enable) 1 else 0))
    }

    private fun enableHeater(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_HEATER, data = if (enable) 1 else 0))
    }

    private fun enablePurifier(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_1, data = if (enable) 1 else 0))
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_PURIFIER_2, data = if (enable) 1 else 0))
    }

    private fun enableBoardLed(enable: Boolean) {
        tankApi.sendCommand(ServerPacket(opCode = SERVER_OP_MEGA_LED, data = if (enable) 1 else 0))
    }

    private fun calculateWaterPumpTime(ratio: Double): Int {
        // TODO - WaterPump 의 분당 출수량을 측정하여 ratio 에 따라 동작시간을 계산하여 리턴

        val targetOutVolume = TANK_WATER_VOLUME * ratio
        return round((targetOutVolume / 2.0) * 60).toInt()
    }

    fun uiEvent(uiEvent: UiEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            when (uiEvent) {
                is UiEvent.OutWaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "${if (uiEvent.value) "Open" else "Close"} Out-Water valve!",
                            outWaterValveState = uiEvent.value,
                        )
                    )
                    enableOutWaterValve(uiEvent.value)
                }
                is UiEvent.InWaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            inWaterValveState = uiEvent.value,
                            resultText = "${if (uiEvent.value) "Open" else "Close"} In-Water valve!"
                        )
                    )
                    enableInWaterValve(uiEvent.value)
                }
                is UiEvent.LightEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            lightState = uiEvent.value,
                            resultText = "Light ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enableLight(uiEvent.value)
                }
                is UiEvent.PumpEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            pumpState = uiEvent.value,
                            resultText = "Pump ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enablePump(uiEvent.value)
                }
                is UiEvent.HeaterEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            heaterState = uiEvent.value,
                            resultText = "Heater ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enableHeater(uiEvent.value)
                }
                is UiEvent.PurifierEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            purifierState = uiEvent.value,
                            resultText = "Purifier ${if (uiEvent.value) "On" else "Off"} "
                        )
                    )
                    enablePurifier(uiEvent.value)
                }
                is UiEvent.ChangeWater -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "Start change-water"
                        )
                    )
                    // TODO - Change this ratio later!
                    changeWater(0.3)
                }
                is UiEvent.LedEvent -> {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            resultText = "${if (uiEvent.value) "On" else "Off"} Board LED"
                        )
                    )
                    enableBoardLed(uiEvent.value)
                }
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