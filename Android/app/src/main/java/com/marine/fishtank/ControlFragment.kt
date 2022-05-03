package com.marine.fishtank

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.marine.fishtank.databinding.FragmentControlBinding
import com.marine.fishtank.model.Status
import com.marine.fishtank.model.TankData
import com.marine.fishtank.viewmodel.ControlViewModel

class ControlFragment : Fragment(), OnChartValueSelectedListener {
    private lateinit var binding: FragmentControlBinding
    private lateinit var viewModel: ControlViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentControlBinding.inflate(inflater, container, false)

        viewModel = ControlViewModel()

        binding.lineChart.apply {
            setOnChartValueSelectedListener(this@ControlFragment)

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

        setData()

        setupObserver()
        viewModel.init()
        viewModel.startListenTank()

        return binding.root
    }

    private fun setupObserver() {
        viewModel.liveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.let { tankData ->
                        addData(tankData)
                    }
                }
                Status.ERROR -> {

                }
                Status.LOADING -> {

                }
            }
        }
    }

    private fun addData(tankData: TankData) {
        val data = binding.lineChart.data
        val set = data.getDataSetByIndex(0)

        data.addEntry(Entry(set.entryCount.toFloat(), tankData.temperature.toFloat()), 0)
        data.notifyDataChanged()

        binding.lineChart.apply {
            notifyDataSetChanged()
            setVisibleXRangeMaximum(15f)
            moveViewToX(data.entryCount.toFloat())
        }
    }

    private fun setData() {
        val entryList = mutableListOf<Entry>()
        entryList.add(Entry(0f, 25f))
        entryList.add(Entry(1f, 25.2f))
        entryList.add(Entry(2f, 25.35f))
        entryList.add(Entry(3f, 25.45f))
        entryList.add(Entry(4f, 25f))
        entryList.add(Entry(5f, 24.8f))
        entryList.add(Entry(6f, 25.7f))

        val dataSet = LineDataSet(entryList, "Water temperature").apply {
            setAxisDependency(AxisDependency.LEFT)
            setColor(ColorTemplate.getHoloBlue())
            setCircleColor(Color.BLACK)
            setLineWidth(3f)
            setCircleRadius(4f)
            setFillAlpha(65)
            setFillColor(ColorTemplate.getHoloBlue())
            setHighLightColor(Color.rgb(110, 117, 117))
            setDrawCircleHole(true)
        }

        // create a data object with the data sets
        val data = LineData(dataSet).apply {
            setValueTextColor(Color.BLACK)
            setValueTextSize(10f)
        }

        //String setter in x-Axis
        val xValues = arrayOf("14:00", "14:05", "14:10", "14:15", "14:20", "14:25", "14:30")
        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xValues)

        binding.lineChart.data = data
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
    }

    override fun onNothingSelected() {
    }


}