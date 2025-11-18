package com.example.letsbowl.ui.home

import android.icu.util.Calendar
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.letsbowl.data.Game
import com.example.letsbowl.data.GameRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: GameRepository): ViewModel() {
    val allGames: LiveData<List<Game>> = repository.allGames.asLiveData()

    //점수 목록을 데이터베이스에 저장하는 함수
    fun insertGames(dateMillis: Long, scores: List<Int>){
        viewModelScope.launch {
            scores.forEach { score ->
                val newGame = Game(dateMillis = dateMillis, score = score)
                repository.insert(newGame)
            }
        }
    }

    //날자 값 정규화
    private fun getNormalizedDate(dateMillis: Long): Long{
        val calendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    // 날자값을 기준으로 게임 점수를 저장
    fun saveGamesForDate(dateMillis: Long, scores: List<Int>){
        viewModelScope.launch {
            val normalizedDate = getNormalizedDate(dateMillis)
            repository.replaceGamesForDate(normalizedDate, scores)
        }
    }

    // 날자값을 기준으로 게임 점수를 load
    suspend fun getGamesForDate(dateMillis: Long): List<Game>{
        val normalizedDate = getNormalizedDate(dateMillis)
        return repository.getGamesForDate(normalizedDate)
    }
}