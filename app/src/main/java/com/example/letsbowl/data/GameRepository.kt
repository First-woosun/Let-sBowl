package com.example.letsbowl.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<Game>> = gameDao.getAllGames()

    //DB에 게임 저장
    suspend fun insert(game: Game){
        gameDao.insertGame(game)
    }

    //날자로 게임 읽어오기
    suspend fun getGamesForDate(dateMillis: Long): List<Game>{
        return gameDao.getGamesForDate(dateMillis)
    }

    //날자의 게임 교체하기
    suspend fun replaceGamesForDate(dateMillis: Long, scores: List<Int>){
        gameDao.replaceGamesForDate(dateMillis, scores)
    }
}