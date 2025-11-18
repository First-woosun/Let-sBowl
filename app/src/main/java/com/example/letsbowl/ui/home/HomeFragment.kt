package com.example.letsbowl.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.letsbowl.data.Game
import com.example.letsbowl.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory = HomeViewModelFactory(requireActivity().application)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lineChart = binding.lineChart
        setupChart() // 차트 초기 설정

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel의 allGames 구독
        homeViewModel.allGames.observe(viewLifecycleOwner) { gamesList ->
            if (gamesList.isNotEmpty()) {
                updateChart(gamesList)
                updateAggregates(gamesList)
            } else {
                lineChart.clear()
                lineChart.invalidate()
                binding.textAverageValue.text = "0"
                binding.textMaxValue.text = "0"
            }
        }

        // "add_game" 버튼 클릭 리스너
        binding.addGame.setOnClickListener {
            val dialog = AddGameDialogFragment()
            dialog.show(childFragmentManager, "AddGameDialogFragment")
        }
    }

    // 어플 실행시 차트를 setup하는 함수
    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.textColor = Color.GRAY
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.valueFormatter = DateAxisValueFormatter()
        
        xAxis.granularity = 1.0f

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.GRAY
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.LTGRAY
        yAxisLeft.granularity = 1.0f
        lineChart.axisRight.isEnabled = false
    }

    //DB의 데이터가 수정되면 차트를 업데이트하는 함수
    private fun updateChart(games: List<Game>) {
        if (games.isEmpty()) {
            lineChart.clear()
            lineChart.invalidate()
            return
        }

        //날짜별 평균 계산 로직
        val gamesByDay: Map<Long, List<Game>> = games.reversed().groupBy { it.dateMillis }
        val entries = ArrayList<Entry>()
        gamesByDay.entries.forEachIndexed { index, (dateMillis, dailyGames) ->
            val dailyAverage = dailyGames.map { it.score }.average()
            if (!dailyAverage.isNaN()) {
                entries.add(Entry(index.toFloat(), dailyAverage.toFloat(), dateMillis))
            }
        }

        val xAxis = lineChart.xAxis


        //X축 좌우 여백 설정
        if (entries.isNotEmpty()) {
            xAxis.axisMinimum = -0.2f
            xAxis.axisMaximum = (entries.size - 1).toFloat() + 0.2f
        }

        //데이터 셋 설정
        val dataSet = LineDataSet(entries, "일일 평균")
        dataSet.color = Color.BLUE
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 3f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.invalidate() // 차트 새로고침
    }

    //전체 평균, 최고 점수를 계산 하고 UI에 업데이트
    private fun updateAggregates(games: List<Game>) {
        if (games.isEmpty()) return

        val averageScore = games.map { it.score }.average()
        binding.textAverageValue.text = String.format("%.0f", averageScore)

        val maxScore = games.maxByOrNull { it.score }?.score ?: 0
        binding.textMaxValue.text = maxScore.toString()
    }

    // x 라벨 포맷터
    inner class DateAxisValueFormatter : ValueFormatter() {
        private val sdf = SimpleDateFormat("MM/dd", Locale.KOREA)

        override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
            try {
                val dataSet = lineChart.data.getDataSetByIndex(0)
                val entry = dataSet.getEntryForIndex(value.toInt())
                val dateMillis = entry.data as Long
                return sdf.format(dateMillis)

            } catch (e: Exception) {
                return ""
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}