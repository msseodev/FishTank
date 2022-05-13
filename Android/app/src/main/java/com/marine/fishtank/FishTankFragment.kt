package com.marine.fishtank

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.marine.fishtank.model.TemperatureData
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.FishTankViewModelFactory
import com.marine.fishtank.viewmodel.UiEvent
import com.marine.fishtank.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FishTankFragment"

@OptIn(ExperimentalPagerApi::class)
class FishTankFragment : Fragment() {
    private val viewModel: FishTankViewModel by viewModels {
        FishTankViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Need call to pull the trigger of Composition.
        viewModel.init()
        viewModel.startFetchHistory()

        viewModel.startListenTemperature()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FishTankScreen(viewModel = viewModel) { uiEvent ->
                        viewModel.uiEvent(uiEvent)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FishTankScreen(viewModel: FishTankViewModel,
                   eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing FishTankScreen")

    val uiState: UiState by viewModel.uiState.observeAsState(UiState())
    val temperatureState: TemperatureData by viewModel.temperatureLiveData.observeAsState(TemperatureData())

    val tabTitles = listOf("Control", "Monitor", "ETC")
    val pagerState = rememberPagerState()

    // Surface = TAB 전체화면
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column {
            TabRow(
                backgroundColor = Color.Cyan,
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions -> // 3.
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(
                            pagerState,
                            tabPositions
                        )
                    )
                }) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(selected = pagerState.currentPage == index,
                        onClick = { CoroutineScope(Dispatchers.Main).launch { pagerState.scrollToPage(index) } },
                        text = { Text(text = title) })
                }
            }
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                count = tabTitles.size,
                state = pagerState,
                verticalAlignment = Alignment.Top
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> ControlTab(uiState, eventHandler)
                    1 -> MonitorPage(temperatureState, eventHandler)
                    2 -> Text("ETC!!!")
                }
            }
        }
    }
}

@Composable
fun MonitorPage(temperatureData: TemperatureData, eventHandler: (UiEvent) -> Unit) {
    Chart(temperatureData, eventHandler)
}

@Composable
fun Chart(temperatureData: TemperatureData, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing Chart!")

    AndroidView(
        modifier = Modifier
            .height(500.dp)
            .fillMaxWidth(),
        factory = { context ->
            LineChart(context).apply {
                // no description text
                description.isEnabled = false

                // enable touch gestures
                setTouchEnabled(true)

                dragDecelerationFrictionCoef = 0.9f

                // enable scaling and dragging
                isDragEnabled = true
                setScaleEnabled(true)
                setDrawGridBackground(false)
                isHighlightPerDragEnabled = true

                // if disabled, scaling can be done on x- and y-axis separately
                setPinchZoom(true)

                // set an alternative background color
                setBackgroundColor(android.graphics.Color.WHITE)

                xAxis.apply {
                    textSize = 11f
                    textColor = android.graphics.Color.BLACK
                    setDrawGridLines(true)
                    setDrawAxisLine(true)
                    position = XAxis.XAxisPosition.BOTTOM

                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val entry = data.dataSets[0].getEntryForIndex(value.toInt())
                            val tankData = entry.data as TemperatureData
                            val date = Date(tankData.dateTime)
                            return SimpleDateFormat("HH:mm:ss").format(date)
                        }
                    }
                }

                axisLeft.apply {
                    textColor = android.graphics.Color.BLACK
                    setDrawGridLines(true)
                    setDrawAxisLine(true)

                    //String setter in x-Axis
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    }
                    //axisMaximum = LineChartConfig.YAXIS_MAX
                    //axisMinimum = LineChartConfig.YAXIS_MIN

                    spaceBottom = 20f
                    spaceTop = 20f
                }

                axisRight.apply {
                    isEnabled = false
                }

                legend.apply {
                    textSize = 12f
                }

                val entryList = mutableListOf<Entry>()
                val dataSet = LineDataSet(entryList, "Water temperature").apply {
                    axisDependency = YAxis.AxisDependency.LEFT
                    color = ColorTemplate.getHoloBlue()
                    setCircleColor(android.graphics.Color.BLACK)
                    lineWidth = 3f
                    circleRadius = 4f
                    fillAlpha = 65
                    fillColor = ColorTemplate.getHoloBlue()
                    highLightColor = android.graphics.Color.rgb(110, 117, 117)
                    setDrawCircleHole(true)
                }

                // create a data object with the data sets
                data = LineData(dataSet).apply {
                    setValueTextColor(android.graphics.Color.BLACK)
                    setValueTextSize(10f)
                    setValueFormatter(object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.2f", value)
                        }
                    })
                }
            }
        },
        update = {
            Log.d(TAG, "Update LineChart")
            if(temperatureData.temperature > 0 ) {
                val dataSet = it.data.getDataSetByIndex(0)

                it.data.addEntry(
                    Entry(
                        dataSet.entryCount.toFloat(), temperatureData.temperature.toFloat(),
                        temperatureData
                    ), 0
                )
                it.data.notifyDataChanged()

                it.notifyDataSetChanged()
                it.setVisibleXRangeMaximum(10f)
                it.moveViewToX(it.data.entryCount.toFloat())
            }
        })
}

@Composable
fun ControlTab(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            text = uiState.resultText,
            textAlign = TextAlign.Center
        )

        Text(text = "Functions")

        Divider(modifier = Modifier.padding(vertical = 5.dp))

        // Create Radio
        RadioGroup(
            listOf(
                RadioState(
                    false,
                    stringResource(R.string.open_water_out)
                ) { eventHandler(UiEvent.OutWaterEvent(true)) },
                RadioState(
                    false,
                    stringResource(R.string.close_water_out)
                ) { eventHandler(UiEvent.OutWaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, stringResource(R.string.open_water_in)) { eventHandler(UiEvent.InWaterEvent(true)) },
                RadioState(false, stringResource(R.string.close_water_in)) { eventHandler(UiEvent.InWaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, stringResource(R.string.pump_run)) { eventHandler(UiEvent.PumpEvent(true)) },
                RadioState(false, stringResource(R.string.pump_stop)) { eventHandler(UiEvent.PumpEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, stringResource(R.string.purifier_on)) { eventHandler(UiEvent.PurifierEvent(true)) },
                RadioState(false, stringResource(R.string.purifier_off)) { eventHandler(UiEvent.PurifierEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, stringResource(R.string.heater_on)) { eventHandler(UiEvent.HeaterEvent(true)) },
                RadioState(false, stringResource(R.string.heater_off)) { eventHandler(UiEvent.HeaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, stringResource(R.string.board_led_on)) { eventHandler(UiEvent.LedEvent(true)) },
                RadioState(false, stringResource(R.string.board_led_off)) { eventHandler(UiEvent.LedEvent(false)) }
            )
        )
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            onClick = { eventHandler(UiEvent.ChangeWater()) }) {
            Text(text = stringResource(id = R.string.change_water))
        }
    }
}

data class RadioState(
    var selected: Boolean = false,
    var text: String,
    var onclick: () -> Unit
)

@Composable
fun RadioGroup(radioList: List<RadioState>) {
    val selectedIndex = remember {
        mutableStateOf(0)
    }

    Row {
        radioList.forEachIndexed { index, radioState ->
            val selected = index == selectedIndex.value
            val onClickHandle = {
                selectedIndex.value = index
                radioState.selected = true

                radioState.onclick()
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = selected, onClick = onClickHandle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClickHandle
                )
                Text(text = radioState.text)
            }
        }
    }
}