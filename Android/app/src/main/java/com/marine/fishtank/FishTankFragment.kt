package com.marine.fishtank

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.marine.fishtank.viewmodel.FishTankViewModel
import com.marine.fishtank.viewmodel.FishTankViewModelFactory
import com.marine.fishtank.viewmodel.UiEvent
import com.marine.fishtank.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "FishTankFragment"

@OptIn(ExperimentalPagerApi::class)
class FishTankFragment: Fragment() {
    private val viewModel: FishTankViewModel by viewModels {
        FishTankViewModelFactory()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    viewModel.uiState.observeAsState().value?.let { uiState ->
                        FishTankScreen(uiState) { uiEvent ->
                            viewModel.uiEvent(uiEvent)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FishTankScreen(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Log.d(TAG, "Composing SecondScreen")

    val tabTitles = listOf("Control", "Monitor", "ETC")
    val pagerState = rememberPagerState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = "AppBar") },
            )
        }) {
            Column {
                TabRow(selectedTabIndex = pagerState.currentPage,
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
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> ControlTab(uiState, eventHandler)
                        1 -> Text("MONITOR!!!")
                        2 -> Text("ETC!!!")
                    }
                }
            }
        }
    }
}


@Composable
fun ControlTab(uiState: UiState, eventHandler: (UiEvent) -> Unit) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            text = uiState.resultText,
            textAlign = TextAlign.Center
        )

        Text(text = "Functions")

        Divider(modifier = Modifier.padding(vertical = 5.dp))

        // Create Radio
        RadioGroup(
            listOf(
                RadioState(false, "Open Out-water valve") { eventHandler(UiEvent.OutWaterEvent(true)) },
                RadioState(false, "Close Out-water valve") { eventHandler(UiEvent.OutWaterEvent(false)) }
            )
        )
        RadioGroup(
            listOf(
                RadioState(false, "Open In-water valve") { eventHandler(UiEvent.InWaterEvent(true)) },
                RadioState(false, "Close In-water valve") { eventHandler(UiEvent.InWaterEvent(false)) }
            )
        )
    }

}

@Composable
fun tab() {
    var tabIndex by remember { mutableStateOf(0) } // 1.
    val tabTitles = listOf("Hello", "There", "World")
    Column { // 2.
        TabRow(selectedTabIndex = tabIndex) { // 3.
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = tabIndex == index, // 4.
                    onClick = { tabIndex = index },
                    text = { Text(text = title) }) // 5.
            }
        }
        when (tabIndex) { // 6.
            0 -> Text("Hello content")
            1 -> Text("There content")
            2 -> Text("World content")
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

@Composable
fun CreateButton(text: String, modifier: Modifier, onclick: () -> Unit) {
    Button(
        onClick = onclick,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

@OptIn(ExperimentalPagerApi::class)
@Preview
@Composable
fun PhotographerCardPreview() {
    MaterialTheme {
        FishTankScreen(
            UiState(
                outWaterValveState = true,
                inWaterValveState = false,
                lightState = true,
                pumpState = true,
                "Fetch complete!"
            )
        )
        { uiEvent ->

        }
    }
}