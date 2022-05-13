package com.marine.fishtank

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.marine.fishtank.databinding.FragmentControlBinding
import com.marine.fishtank.model.Status
import com.marine.fishtank.model.TankData
import com.marine.fishtank.viewmodel.ControlViewModelLegacy
import java.text.SimpleDateFormat
import java.util.*

object LineChartConfig {
    const val YAXIS_MAX = 35.0f
    const val YAXIS_MIN = 20.0f

    const val POINT_COUNT_MAXIMUM = 10f
}

class ControlFragmentLegacy : Fragment(), OnChartValueSelectedListener, View.OnClickListener {
    private lateinit var binding: FragmentControlBinding
    private lateinit var viewModel: ControlViewModelLegacy

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentControlBinding.inflate(inflater, container, false)

        viewModel = ControlViewModelLegacy()

        binding.buttonChangeWater.setOnClickListener(this)
        binding.buttonHeaterOff.setOnClickListener(this)
        binding.buttonHeaterOn.setOnClickListener(this)
        binding.buttonLightOff.setOnClickListener(this)
        binding.buttonLightOn.setOnClickListener(this)
        binding.buttonPurifierOff.setOnClickListener(this)
        binding.buttonPurifierOn.setOnClickListener(this)

        binding.lineChart.apply {
            setOnChartValueSelectedListener(this@ControlFragmentLegacy)

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
            setBackgroundColor(Color.WHITE)

            xAxis.apply {
                textSize = 11f
                textColor = Color.BLACK
                setDrawGridLines(true)
                setDrawAxisLine(true)
                position = XAxis.XAxisPosition.BOTTOM
            }

            axisLeft.apply {
                textColor = Color.BLACK
                setDrawGridLines(true)
                setDrawAxisLine(true)
            }

            axisRight.apply {
                isEnabled = false
            }

            legend.apply {
                textSize = 12f
            }
        }

        prepareDataChart()

        setupObserver()
        viewModel.init()
        viewModel.startFetchHistory()

        viewModel.startListenTemperature()

        return binding.root
    }

    private fun setupObserver() {
        viewModel.initData.observe(viewLifecycleOwner) {
            when(it.status) {
                Status.SUCCESS -> Toast.makeText(context?.applicationContext, "Initialize success.", Toast.LENGTH_SHORT).show()
                Status.ERROR -> Toast.makeText(context?.applicationContext, "Initialize fail! " + it.data, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.liveTankData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { tankData ->
                        addData(tankData)
                    }
                }
            }
        }

        viewModel.temperatureData.observe(viewLifecycleOwner) {
            addData(
                TankData(it, false, false, false, System.currentTimeMillis())
            )
        }
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.buttonBoardLedToggle -> {
                viewModel.toggleBoardLed()
            }
            R.id.buttonChangeWater -> {
                val ratioText = binding.editTextWaterRatio.text.toString()
                viewModel.changeWater(Integer.parseInt(ratioText) * 0.01)
            }
            R.id.buttonPurifierOn -> {
                viewModel.enablePurifier(true)
            }
            R.id.buttonPurifierOff -> {
                viewModel.enablePurifier(false)
            }
            R.id.buttonLightOn -> {
                viewModel.enableLight(true)
            }
            R.id.buttonLightOff -> {
                viewModel.enableLight(false)
            }
            R.id.buttonHeaterOn -> {
                viewModel.enableHeater(true)
            }
            R.id.buttonHeaterOff -> {
                viewModel.enableHeater(false)
            }
        }
    }

    private fun addData(tankData: TankData) {
        val data = binding.lineChart.data
        val set = data.getDataSetByIndex(0)

        data.addEntry(Entry(set.entryCount.toFloat(), tankData.temperature.toFloat(), tankData), 0)
        data.notifyDataChanged()

        binding.lineChart.apply {
            notifyDataSetChanged()
            setVisibleXRangeMaximum(LineChartConfig.POINT_COUNT_MAXIMUM)
            moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun prepareDataChart() {
        val entryList = mutableListOf<Entry>()
        val dataSet = LineDataSet(entryList, "Water temperature").apply {
            axisDependency = AxisDependency.LEFT
            color = ColorTemplate.getHoloBlue()
            setCircleColor(Color.BLACK)
            lineWidth = 3f
            circleRadius = 4f
            fillAlpha = 65
            fillColor = ColorTemplate.getHoloBlue()
            highLightColor = Color.rgb(110, 117, 117)
            setDrawCircleHole(true)
        }

        // create a data object with the data sets
        val data = LineData(dataSet).apply {
            setValueTextColor(Color.BLACK)
            setValueTextSize(10f)
            setValueFormatter(object: ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f", value)
                }
            })
        }

        //String setter in x-Axis
        binding.lineChart.axisLeft.apply {
            valueFormatter = yAxisFormatter
            axisMaximum = LineChartConfig.YAXIS_MAX
            axisMinimum = LineChartConfig.YAXIS_MIN
        }

        binding.lineChart.xAxis.valueFormatter = xAxisFormatter
        binding.lineChart.data = data
    }

    private val xAxisFormatter = object: ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val entry = binding.lineChart.data.dataSets[0].getEntryForIndex(value.toInt())
            val tankData = entry.data as TankData
            val date = Date(tankData.dateTime)
            return SimpleDateFormat("HH:mm:ss").format(date)
        }
    }

    private val yAxisFormatter = object: ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.2f", value)
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }


}