package com.example.letsbowl.ui.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.letsbowl.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddGameDialogFragment : DialogFragment() {

    // 1. 리스너 인터페이스 변경: 점수 "목록"을 전달
    interface AddGameDialogListener {
        fun onGamesAdded(dateMillis: Long, scores: List<Int>)
    }

    private var listener: AddGameDialogListener? = null
    private val selectedCalendar = Calendar.getInstance()

    // 점수 입력 뷰들을 담을 부모 레이아웃
    private var scoresContainer: LinearLayout? = null
    // 인플레이터
    private var inflater: LayoutInflater? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        try {
            listener = parentFragment as AddGameDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((parentFragment.toString() + " must implement AddGameDialogListener"))
        }

        inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(requireActivity())
        val view = inflater!!.inflate(R.layout.dialog_add_game, null)

        // 뷰 초기화
        val textSelectedDate = view.findViewById<TextView>(R.id.text_selected_date)
        val buttonAddScoreView = view.findViewById<Button>(R.id.button_add_score_view)
        val buttonCancel = view.findViewById<Button>(R.id.button_cancel)
        val buttonSave = view.findViewById<Button>(R.id.button_save)
        scoresContainer = view.findViewById(R.id.container_scores) // 컨테이너 초기화

        // 날짜 선택 텍스트뷰 초기값
        updateDateText(textSelectedDate)

        // 날짜 선택 리스너
        textSelectedDate.setOnClickListener {
            showDatePicker(textSelectedDate)
        }

        // 2. "+" (게임 추가) 버튼 리스너
        buttonAddScoreView.setOnClickListener {
            addScoreView() // 점수 입력 뷰 추가
        }

        // 취소 버튼
        buttonCancel.setOnClickListener {
            dismiss()
        }

        // 3. "입력" (저장) 버튼 리스너
        buttonSave.setOnClickListener {
            val scores = mutableListOf<Int>()
            var allScoresValid = true

            // 4. 컨테이너 내의 모든 점수 뷰를 순회하며 값 수집
            for (i in 0 until (scoresContainer?.childCount ?: 0)) {
                val scoreView = scoresContainer?.getChildAt(i)
                val editScore = scoreView?.findViewById<EditText>(R.id.edit_score_item)
                val scoreString = editScore?.text.toString()

                if (scoreString.isNotEmpty()) {
                    val score = scoreString.toInt()
                    if (score in 0..300) {
                        scores.add(score)
                    } else {
                        // 유효성 검사 실패
                        editScore?.error = "0-300 사이"
                        allScoresValid = false
                    }
                } else {
                    // 비어있는 필드가 있으면 안됨
                    editScore?.error = "점수 입력"
                    allScoresValid = false
                }
            }

            if (scores.isEmpty()) {
                Toast.makeText(context, "최소 1개 이상의 점수를 입력하세요.", Toast.LENGTH_SHORT).show()
                // 최소 1개는 있어야 한다면, 여기서 addScoreView()를 호출해둘 수 있습니다.
                return@setOnClickListener
            }

            if (allScoresValid) {
                // 모든 점수가 유효하면 리스너로 전달
                listener?.onGamesAdded(selectedCalendar.timeInMillis, scores)
                dismiss()
            } else {
                Toast.makeText(context, "유효하지 않은 점수가 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 5. 다이얼로그가 처음 뜰 때 기본으로 1개의 점수 입력란을 추가
        addScoreView()

        builder.setView(view)
        return builder.create()
    }

    // 6. 점수 입력 뷰를 동적으로 추가하는 함수
    private fun addScoreView() {
        if (inflater == null || scoresContainer == null) return

        val scoreView = inflater!!.inflate(R.layout.item_game_score, scoresContainer, false)

        val buttonRemove = scoreView.findViewById<ImageButton>(R.id.button_remove_item)
        buttonRemove.setOnClickListener {
            // "x" 버튼 클릭 시 해당 뷰 삭제
            scoresContainer?.removeView(scoreView)
        }

        scoresContainer?.addView(scoreView)
    }

    // 날짜 선택 다이얼로그 (이전과 동일)
    private fun showDatePicker(dateTextView: TextView) {
        val cal = selectedCalendar
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateText(dateTextView)
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // 날짜 텍스트 업데이트 (이전과 동일)
    private fun updateDateText(dateTextView: TextView) {
        val format = "yyyy년 MM월 dd일"
        val sdf = SimpleDateFormat(format, Locale.KOREA)
        dateTextView.text = sdf.format(selectedCalendar.time)
    }
}