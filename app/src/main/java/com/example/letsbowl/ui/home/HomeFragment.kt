package com.example.letsbowl.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider // 1. ViewModelProvider 임포트
import com.example.letsbowl.ui.home.AddGameDialogFragment
import com.example.letsbowl.data.Game // 2. Game 임포트
import com.example.letsbowl.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter // 3. ValueFormatter 임포트
import java.text.SimpleDateFormat // 4. SimpleDateFormat 임포트
import java.util.Locale

class HomeFragment : Fragment(), AddGameDialogFragment.AddGameDialogListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 5. homeViewModel을 'lateinit'으로 변경
    private lateinit var homeViewModel: HomeViewModel

    // 6. LineChart를 클래스 멤버 변수로 이동
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 7. ViewModelFactory를 사용해 ViewModel 생성 (중요!)
        val factory = HomeViewModelFactory(requireActivity().application)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 8. 차트 뷰 바인딩
        lineChart = binding.lineChart
        setupChart() // 차트 초기 설정 함수 호출

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 9. ViewModel의 allGames (LiveData)를 구독(Observe)합니다.
        // DB 데이터가 변경될 때마다 이 블록이 자동으로 실행됩니다.
        homeViewModel.allGames.observe(viewLifecycleOwner) { gamesList ->
            // 10. gamesList (DB에서 가져온 List<Game>)로 차트와 UI를 업데이트합니다.
            Log.d("HomeFragment", "liveData 갱신 리스트 크기: ${gamesList.size}")
            if (gamesList.isNotEmpty()) {
                updateChart(gamesList) // 차트 데이터 업데이트
                updateAggregates(gamesList) // 평균, 최고점 등 업데이트
            } else {
                // 데이터가 없을 때 처리 (예: 차트 초기화)
                lineChart.clear()
                binding.textAverageValue.text = "0"
                binding.textMaxValue.text = "0" // (max_score_layout의 ID가 text_max_value라고 가정)
            }
        }

        // "add_game" 버튼 클릭 리스너 (이전과 동일)
        binding.addGame.setOnClickListener {
            val dialog = AddGameDialogFragment()
            dialog.show(childFragmentManager, "AddGameDialogFragment")
        }
    }

    /**
     * 11. 다이얼로그의 '입력' 버튼을 눌렀을 때 호출되는 콜백 (수정)
     * 이 데이터를 ViewModel로 전달합니다.
     */
    override fun onGamesAdded(dateMillis: Long, scores: List<Int>) {
        Log.d("HomeFragment", "새 게임 추가됨 - 날짜: $dateMillis, 점수 목록: ${scores.joinToString()}")

        // 12. ViewModel의 insertGames 함수 호출
        homeViewModel.insertGames(dateMillis, scores)
    }

    /**
     * 차트의 기본 스타일을 설정하는 함수
     */
    private fun setupChart() {
        // (기존 onViewCreated에 있던 차트 설정 코드를 여기로 이동)
        lineChart.description.isEnabled = false
        lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.textColor = Color.GRAY
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        // 13. X축 포맷터 설정 (날짜 표시)
        xAxis.valueFormatter = DateAxisValueFormatter()

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.GRAY
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.LTGRAY
        lineChart.axisRight.isEnabled = false
    }

    /**
     * 14. DB에서 가져온 데이터(List<Game>)로 차트를 업데이트하는 함수
     */
    private fun updateChart(games: List<Game>) {
        // 15. 데이터를 Entry 리스트로 변환 (날짜 역순으로 정렬된 상태)
        // 차트는 시간 순서(오름차순)으로 그리는 것이 자연스러우므로 reversed() 사용
        val entries = ArrayList<Entry>()
        games.reversed().forEachIndexed { index, game ->
            // X축: 인덱스 (0, 1, 2...)
            // Y축: 점수 (game.score)
            // data: 날짜 (game.dateMillis) - X축 라벨 포매팅에 사용
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
        lineChart.invalidate() // 차트 새로고침
    }

    /**
     * 15. 평균, 최고점 등을 계산하고 UI에 표시하는 함수
     */
    private fun updateAggregates(games: List<Game>) {
        if (games.isEmpty()) return

        // 주간 평균 계산 (예시: 여기서는 전체 평균으로 대체)
        val averageScore = games.map { it.score }.average()
        binding.textAverageValue.text = String.format("%.0f", averageScore)

        // 최고점 계산
        val maxScore = games.maxByOrNull { it.score }?.score ?: 0
        binding.textMaxValue.text = maxScore.toString() // (XML의 ID가 text_max_value라고 가정)
    }

    /**
     * 16. X축 라벨을 날짜(Long)에서 'MM/dd' 형식으로 변환하는 포맷터
     */
    inner class DateAxisValueFormatter : ValueFormatter() {
        private val sdf = SimpleDateFormat("MM/dd", Locale.KOREA)

        override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
            // value는 X축의 인덱스(0.0f, 1.0f...)입니다.
            // dataSet에서 해당 인덱스의 Entry를 찾아 날짜(data)를 가져옵니다.
            try {
                val dataSet = lineChart.data.getDataSetByIndex(0)
                val entry = dataSet.getEntryForIndex(value.toInt())

                // Entry 생성 시 data 필드에 저장한 dateMillis (Long) 값을 사용
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