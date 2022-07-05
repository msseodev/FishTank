package com.marine.fishtank

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.marine.fishtank.model.PeriodicTask
import com.marine.fishtank.model.Temperature
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import kotlin.random.Random

private const val TAG = "FishTankTest"

@RunWith(AndroidJUnit4::class)
class FishTankTest {
    private val appContext by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel by lazy { FishTankViewModel(FishApplication.INSTANCE!!) }

    @Test
    fun brightnessTest() {
        runBlocking {
            var fakeUiState = MutableLiveData<UiState>()
            var fakeTemperature = MutableLiveData<List<Temperature>>()
            var fakePeriodicTask = MutableLiveData<List<PeriodicTask>>()

            // Start the app
            composeTestRule.setContent {
                MaterialTheme {
                    val uiState: UiState by fakeUiState.observeAsState(UiState())
                    val temperatureState: List<Temperature> by fakeTemperature.observeAsState(emptyList())
                    val periodicTasks: List<PeriodicTask> by fakePeriodicTask.observeAsState(emptyList())

                    FishTankScreen(
                        uiState = uiState,
                        temperatureState = temperatureState,
                        periodicTasks = periodicTasks
                    ) { uiEvent ->
                        Log.d(TAG, "uiEvent=$uiEvent")
                    }
                }
            }

            val random = Random(System.currentTimeMillis())
            repeat(10) {
                val brValue = random.nextInt(100)
                Log.d(TAG, "Set brightness to $brValue")
                fakeUiState.postValue(
                    fakeUiState.value?.copy(brightness = brValue)
                )

                delay(1000)
            }
        }
    }
}