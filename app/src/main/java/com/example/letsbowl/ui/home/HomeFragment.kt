package com.example.letsbowl.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log // 1. Log 임포트 추가
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.letsbowl.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis

// 2. AddGameDialogFragment 임포트 추가
// (이 파일이 'com.example.letsbowl' 패키지 바로 아래 있다고 가정합니다.)
// (만약 'ui' 폴더나 다른 곳에 있다면 경로를 수정해주세요.)

// 3. 리스너 인터페이스를 구현(implements)하도록 수정
class HomeFragment : Fragment(), AddGameDialogFragment.AddGameDialogListener {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pageLabel: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner){  }

        val avgScoreValue : TextView = binding.textAverageValue
        homeViewModel.text.observe(viewLifecycleOwner) {  }

        val avgScorePerGame: TextView = binding.textAverageLabel
        homeViewModel.text.observe(viewLifecycleOwner) {  }

        // ... (차트 코드 상단은 동일) ...
        val lineChart: LineChart = binding.lineChart
        val entries = ArrayList<Entry>()
        val dataSet = LineDataSet(entries, "My bowl Average")
        dataSet.color = Color.BLUE
        dataSet.setDrawValues(false)
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 5f

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.textColor = Color.GRAY
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.textColor = Color.GRAY
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = Color.LTGRAY
        lineChart.axisRight.isEnabled = false

        lineChart.legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
        lineChart.legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
        lineChart.description.isEnabled = false

        lineChart.invalidate()

        // --- 💡 여기에 추가된 코드 💡 ---
        // 4. "add_game" 버튼 클릭 리스너 설정
        binding.addGame.setOnClickListener {
            val dialog = AddGameDialogFragment()
            // HomeFragment가 다이얼로그의 부모가 되므로 childFragmentManager 사용
            dialog.show(childFragmentManager, "AddGameDialogFragment")
        }
        // --- 💡 여기까지 ---
    }

    // --- 💡 여기에 추가된 코드 💡 ---
    // 5. 다이얼로그 리스너 구현 메서드 추가
    // (AddGameDialogFragment에서 '입력' 버튼을 누르면 여기가 호출됩니다)
    override fun onGamesAdded(dateMillis: Long, scores: List<Int>) {
        // "입력" 버튼을 눌렀을 때 호출됩니다.
        // 여기서 날짜와 점수 리스트를 받아서 처리합니다.
        Log.d("HomeFragment", "새 게임 추가됨 - 날짜: $dateMillis, 점수 목록: ${scores.joinToString()}")

        // TODO:
        // 1. 이 데이터를 ViewModel로 전달
        // 2. ViewModel에서 DB에 저장
        // 3. DB 저장 후 LiveData 갱신 -> 차트 업데이트 등의 로직 수행
    }
    // --- 💡 여기까지 ---

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}