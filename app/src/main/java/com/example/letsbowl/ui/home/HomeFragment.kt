package com.example.letsbowl.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

class HomeFragment : Fragment(), AddGameDialogFragment.AddGameDialogListener {

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

        // 차트 초기 설정 함수 호출
        setupChart()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //ViewModel의 allGames 구독, DB가 변경될 때마다 자동으로 실행
        homeViewModel.allGames.observe(viewLifecycleOwner) { gamesList ->
            if (gamesList.isNotEmpty()) {
                updateChart(gamesList) // 차트 데이터 업데이트
                updateAggregates(gamesList) // 평균, 최고점 등 업데이트
            } else {
                // 데이터가 없을 때 처리
                lineChart.clear()
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

    //다이얼로그의 '입력' 버튼을 눌렀을 때 호출
    override fun onGamesAdded(dateMillis: Long, scores: List<Int>) {
        Log.d("HomeFragment", "새 게임 추가됨 - 날짜: $dateMillis, 점수 목록: ${scores.joinToString()}")
        homeViewModel.insertGames(dateMillis, scores)
    }

    //차트 초기화
    private fun setupChart() {
        lineChart.description.isEnabled = false
        lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.textColor = Color.GRAY
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        // x축 날자 설정
        xAxis.valueFormatter = DateAxisValueFormatter()

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.GRAY
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.LTGRAY
        lineChart.axisRight.isEnabled = false
    }

    //DB의 데이터를 차트에 업데이트
    private fun updateChart(games: List<Game>) {
        val entries = ArrayList<Entry>()
        games.reversed().forEachIndexed { index, game ->
            entries.add(Entry(index.toFloat(), game.score.toFloat(), game.dateMillis))
        }

        val dataSet = LineDataSet(entries, "내 점수")
        dataSet.color = Color.BLUE
        dataSet.setDrawValues(false)
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 3f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.invalidate()
    }

    //평균, 최고 점수를 계산하고 UI 업데이트
    private fun updateAggregates(games: List<Game>) {
        if (games.isEmpty()) return

        // 전체 평균 계산
        val averageScore = games.map { it.score }.average()
        binding.textAverageValue.text = String.format("%.0f", averageScore)

        // 최고점 계산
        val maxScore = games.maxByOrNull { it.score }?.score ?: 0
        binding.textMaxValue.text = maxScore.toString()
    }

    //x 라벨 포멧터
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