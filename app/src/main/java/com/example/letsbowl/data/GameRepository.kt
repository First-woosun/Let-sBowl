package com.example.letsbowl.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<Game>> = gameDao.getAllGames()

    suspend fun insert(game: Game){
        gameDao.insertGame(game)
    }
}