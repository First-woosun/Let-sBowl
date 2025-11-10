package com.example.letsbowl.ui.home

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
}