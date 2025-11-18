package com.example.letsbowl.ui.home

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.letsbowl.data.Game
import com.example.letsbowl.R
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddGameDialogFragment : DialogFragment() {

    private val selectedCalendar = Calendar.getInstance()
    private var scoresContainer: LinearLayout? = null
    private var inflater: LayoutInflater? = null

    // HomeFragment와 ViewModel을 공유
    private val homeViewModel: HomeViewModel by lazy {
        val factory = HomeViewModelFactory(requireActivity().application)
        ViewModelProvider(requireParentFragment(), factory).get(HomeViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        inflater = requireActivity().layoutInflater
        val builder = AlertDialog.Builder(requireActivity())
        val view = inflater!!.inflate(R.layout.dialog_add_game, null)

        val textSelectedDate = view.findViewById<TextView>(R.id.text_selected_date)
        val buttonAddScoreView = view.findViewById<Button>(R.id.button_add_score_view)
        val buttonCancel = view.findViewById<Button>(R.id.button_cancel)
        val buttonSave = view.findViewById<Button>(R.id.button_save)
        scoresContainer = view.findViewById(R.id.container_scores)

        updateDateText(textSelectedDate)

        // 날짜 선택 리스너 (키보드 숨기기 포함)
        textSelectedDate.setOnClickListener {
            hideKeyboard()
            showDatePicker(textSelectedDate)
        }

        // "+" (게임 추가) 버튼 리스너
        buttonAddScoreView.setOnClickListener {
            val newEditText = addScoreView(null)
            newEditText?.let { showKeyboard(it) }
        }

        // 취소 버튼
        buttonCancel.setOnClickListener {
            dismiss()
        }

        //입력 버튼 리스너
        buttonSave.setOnClickListener {
            val scores = mutableListOf<Int>()
            var allScoresValid = true

            for (i in 0 until (scoresContainer?.childCount ?: 0)) {
                val scoreView = scoresContainer?.getChildAt(i)
                val editScore = scoreView?.findViewById<EditText>(R.id.edit_score_item)
                val scoreString = editScore?.text.toString()

                if (scoreString.isNotEmpty()) {
                    val score = scoreString.toIntOrNull()

                    if (score != null && score in 0..300) {
                        scores.add(score)
                    } else {
                        editScore?.error = "0-300 사이"
                        allScoresValid = false
                    }
                }
            }

            // 유효성 검사
            if (allScoresValid) {
                homeViewModel.saveGamesForDate(selectedCalendar.timeInMillis, scores)
                dismiss()
            } else {
                Toast.makeText(context, "유효하지 않은 점수가 있습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        val initialEditText = addScoreView(null)
        loadDataForDate(selectedCalendar.timeInMillis) // 비동기 로드

        builder.setView(view)
        val dialog = builder.create()

        initialEditText?.let {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            showKeyboard(it)
        }

        return dialog
    }

    //키보드를 숨기는 함수
    private fun hideKeyboard() {

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(dialog?.window?.currentFocus?.windowToken, 0)
    }

    //해당 날자에 먼저 저장되어있던 점수 load
    private fun loadDataForDate(dateMillis: Long) {

        lifecycleScope.launch {
            val games: List<Game> = homeViewModel.getGamesForDate(dateMillis)

            scoresContainer?.removeAllViews()

            if (games.isNotEmpty()) {
                games.forEach { game ->
                    addScoreView(game.score)
                }
            } else {
                addScoreView(null)
            }
        }
    }
    
    //점수 입력 EditTextView 추가 메서드
    private fun addScoreView(score: Int?): EditText? {
        if (inflater == null || scoresContainer == null) return null

        val scoreView = inflater!!.inflate(R.layout.item_game_score, scoresContainer, false)
        val editScore = scoreView.findViewById<EditText>(R.id.edit_score_item)
        val buttonRemove = scoreView.findViewById<ImageButton>(R.id.button_remove_item)

        if (score != null) {
            editScore.setText(score.toString())
        }

        buttonRemove.setOnClickListener {
            scoresContainer?.removeView(scoreView)
        }
        scoresContainer?.addView(scoreView)

        return editScore
    }
    
    //키보드를 화면에 표시하는 함수
    private fun showKeyboard(editText: EditText) {
        editText.post {
            editText.requestFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    //날자 선택 다이얼로그
    private fun showDatePicker(dateTextView: TextView) {
        val cal = selectedCalendar
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateText(dateTextView)
            loadDataForDate(cal.timeInMillis)
        }

        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    //날자 텍스트 업데이트
    private fun updateDateText(dateTextView: TextView) {
        val format = "yyyy년 MM월 dd일"
        val sdf = SimpleDateFormat(format, Locale.KOREA)
        dateTextView.text = sdf.format(selectedCalendar.time)
    }
}